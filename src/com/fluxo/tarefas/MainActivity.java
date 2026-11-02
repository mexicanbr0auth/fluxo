package com.fluxo.tarefas;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fluxo.tarefas.data.Prefs;
import com.fluxo.tarefas.data.Store;
import com.fluxo.tarefas.model.Task;
import com.fluxo.tarefas.ui.BarChartView;
import com.fluxo.tarefas.ui.Dates;
import com.fluxo.tarefas.ui.MeterView;
import com.fluxo.tarefas.ui.Palette;
import com.fluxo.tarefas.ui.ProgressRingView;
import com.fluxo.tarefas.ui.SwitchView;
import com.fluxo.tarefas.ui.Theme;
import com.fluxo.tarefas.ui.Ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_TODAY = 1;
    private static final int FILTER_WEEK = 2;
    private static final int FILTER_OVERDUE = 3;
    private static final int FILTER_DONE = 4;
    private static final String[] FILTER_LABELS = {"Todas", "Hoje", "Semana", "Atrasadas", "Concluídas"};

    private Palette p;
    private int currentPage = 0;
    private int filter = FILTER_ALL;
    private boolean resumedOnce = false;
    private final List<TextView> chipViews = new ArrayList<>();

    private FrameLayout pageHost;
    private View pageTasks, pageStats, pageSettings;
    private ScrollView tasksScroll, statsScroll, settingsScroll;
    private LinearLayout tasksContent, tasksList, filterChips, emptyState, progressCard;
    private TextView greeting, tasksTitle, tasksSubtitle;
    private ProgressRingView progressRing;
    private TextView progressTitle, progressSub, statTodayValue, statOverdueValue, statHighValue;
    private EditText searchInput;
    private LinearLayout fab;
    private ImageView fabIcon;
    private TextView fabLabel;
    private TextView emptyTitle, emptySub, emptyAction;
    private ImageView emptyIcon;

    private LinearLayout bottomNav, navTasks, navStats, navSettings;
    private FrameLayout navTasksPill, navStatsPill, navSettingsPill;
    private ImageView navTasksIcon, navStatsIcon, navSettingsIcon;
    private TextView navTasksLabel, navStatsLabel, navSettingsLabel;

    private LinearLayout statsContent, statsHero, chartCard, priorityCard, categoryCard;
    private LinearLayout priorityList, categoryList;
    private ProgressRingView statsRing;
    private TextView statsTitle, statsSubtitle, statsMotivation, statsDoneValue,
            statsPendingValue, statsOverdueValue, chartTitle, priorityCardTitle, categoryCardTitle;
    private BarChartView chart;

    private LinearLayout settingsContent, settingsBody;
    private TextView settingsTitle, settingsSubtitle;

    private boolean entranceDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean dark = Theme.isDark(this);
        setTheme(dark ? R.style.Theme_Fluxo_Dark : R.style.Theme_Fluxo);
        super.onCreate(savedInstanceState);
        p = Theme.palette(this);
        Theme.setupWindow(this, p);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("page", 0);
            filter = savedInstanceState.getInt("filter", FILTER_ALL);
        }

        bindViews();
        applyTheme();
        buildFilterChips();
        applyInsets();
        setListeners();

        refreshAll(false);
        selectPage(currentPage, false);
        animateEntrance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Palette np = Theme.palette(this);
        if (np.seed != p.seed || np.dark != p.dark) {
            recreate();
            return;
        }
        if (!resumedOnce) {
            resumedOnce = true;
            return;
        }
        refreshAll(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("page", currentPage);
        outState.putInt("filter", filter);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (currentPage != 0) {
            selectPage(0, true);
            return;
        }
        super.onBackPressed();
    }

    // ------------------------------------------------------------------ binding

    private void bindViews() {
        pageHost = findViewById(R.id.page_host);
        pageTasks = findViewById(R.id.page_tasks);
        pageStats = findViewById(R.id.page_stats);
        pageSettings = findViewById(R.id.page_settings);

        tasksScroll = findViewById(R.id.tasks_scroll);
        tasksContent = findViewById(R.id.tasks_content);
        tasksList = findViewById(R.id.tasks_list);
        filterChips = findViewById(R.id.filter_chips);
        emptyState = findViewById(R.id.empty_state);
        progressCard = findViewById(R.id.progress_card);
        greeting = findViewById(R.id.greeting);
        tasksTitle = findViewById(R.id.tasks_title);
        tasksSubtitle = findViewById(R.id.tasks_subtitle);
        progressRing = findViewById(R.id.progress_ring);
        progressTitle = findViewById(R.id.progress_title);
        progressSub = findViewById(R.id.progress_sub);
        statTodayValue = findViewById(R.id.stat_today_value);
        statOverdueValue = findViewById(R.id.stat_overdue_value);
        statHighValue = findViewById(R.id.stat_high_value);
        searchInput = findViewById(R.id.search_input);
        fab = findViewById(R.id.fab);
        fabIcon = findViewById(R.id.fab_icon);
        fabLabel = findViewById(R.id.fab_label);
        emptyTitle = findViewById(R.id.empty_title);
        emptySub = findViewById(R.id.empty_sub);
        emptyAction = findViewById(R.id.empty_action);
        emptyIcon = findViewById(R.id.empty_icon);

        bottomNav = findViewById(R.id.bottom_nav);
        navTasks = findViewById(R.id.nav_tasks);
        navStats = findViewById(R.id.nav_stats);
        navSettings = findViewById(R.id.nav_settings);
        navTasksPill = findViewById(R.id.nav_tasks_pill);
        navStatsPill = findViewById(R.id.nav_stats_pill);
        navSettingsPill = findViewById(R.id.nav_settings_pill);
        navTasksIcon = findViewById(R.id.nav_tasks_icon);
        navStatsIcon = findViewById(R.id.nav_stats_icon);
        navSettingsIcon = findViewById(R.id.nav_settings_icon);
        navTasksLabel = findViewById(R.id.nav_tasks_label);
        navStatsLabel = findViewById(R.id.nav_stats_label);
        navSettingsLabel = findViewById(R.id.nav_settings_label);

        statsScroll = findViewById(R.id.page_stats);
        statsContent = findViewById(R.id.stats_content);
        statsHero = findViewById(R.id.stats_hero);
        chartCard = findViewById(R.id.chart_card);
        priorityCard = findViewById(R.id.priority_card);
        categoryCard = findViewById(R.id.category_card);
        priorityList = findViewById(R.id.priority_list);
        categoryList = findViewById(R.id.category_list);
        statsRing = findViewById(R.id.stats_ring);
        statsTitle = findViewById(R.id.stats_title);
        statsSubtitle = findViewById(R.id.stats_subtitle);
        statsMotivation = findViewById(R.id.stats_motivation);
        statsDoneValue = findViewById(R.id.stats_done_value);
        statsPendingValue = findViewById(R.id.stats_pending_value);
        statsOverdueValue = findViewById(R.id.stats_overdue_value);
        chartTitle = findViewById(R.id.chart_title);
        priorityCardTitle = findViewById(R.id.priority_card_title);
        categoryCardTitle = findViewById(R.id.category_card_title);
        chart = findViewById(R.id.chart);

        settingsScroll = findViewById(R.id.page_settings);
        settingsContent = findViewById(R.id.settings_content);
        settingsBody = findViewById(R.id.settings_body);
        settingsTitle = findViewById(R.id.settings_title);
        settingsSubtitle = findViewById(R.id.settings_subtitle);
    }

    // ------------------------------------------------------------------ theme

    private void applyTheme() {
        findViewById(R.id.main_root).setBackgroundColor(p.surface);
        statsScroll.setBackgroundColor(p.surface);
        settingsScroll.setBackgroundColor(p.surface);
        tasksScroll.setBackgroundColor(p.surface);
        pageTasks.setBackgroundColor(p.surface);

        greeting.setTextColor(p.onSurfaceVariant);
        greeting.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        tasksTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        tasksTitle.setTextColor(p.onSurface);
        tasksSubtitle.setTextColor(p.onSurfaceVariant);

        progressCard.setBackground(Ui.shape(this, p.primaryContainer, 30));
        progressCard.setPadding(Ui.dp(this, 20), Ui.dp(this, 18),
                Ui.dp(this, 20), Ui.dp(this, 16));
        progressRing.setColors(p.primary, Ui.withAlpha(p.onPrimaryContainer, 0x3A),
                p.onPrimaryContainer);
        progressTitle.setTextColor(p.onPrimaryContainer);
        progressTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        progressSub.setTextColor(Ui.withAlpha(p.onPrimaryContainer, 0xCC));
        statTodayValue.setTextColor(p.onPrimaryContainer);
        statOverdueValue.setTextColor(p.onPrimaryContainer);
        statHighValue.setTextColor(p.onPrimaryContainer);
        statTodayValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        statOverdueValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        statHighValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        ((TextView) findViewById(R.id.stat_today_label)).setTextColor(
                Ui.withAlpha(p.onPrimaryContainer, 0xCC));
        ((TextView) findViewById(R.id.stat_overdue_label)).setTextColor(
                Ui.withAlpha(p.onPrimaryContainer, 0xCC));
        ((TextView) findViewById(R.id.stat_high_label)).setTextColor(
                Ui.withAlpha(p.onPrimaryContainer, 0xCC));
        findViewById(R.id.progress_divider).setBackgroundColor(
                Ui.withAlpha(p.onPrimaryContainer, 0x33));
        findViewById(R.id.stat_div_1).setBackgroundColor(
                Ui.withAlpha(p.onPrimaryContainer, 0x33));
        findViewById(R.id.stat_div_2).setBackgroundColor(
                Ui.withAlpha(p.onPrimaryContainer, 0x33));

        searchInput.setBackground(Ui.shape(this, p.surfaceContainerHigh, 16));
        searchInput.setTextColor(p.onSurface);
        searchInput.setHintTextColor(p.onSurfaceVariant);
        searchInput.setTextSize(14f);
        searchInput.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        searchInput.setCompoundDrawablePadding(Ui.dp(this, 10));
        Drawable searchIcon = getDrawable(R.drawable.ic_search);
        if (searchIcon != null) {
            searchIcon.setTint(p.onSurfaceVariant);
            searchInput.setCompoundDrawablesRelativeWithIntrinsicBounds(searchIcon, null, null, null);
        }

        fab.setBackground(Ui.rippleBg(this, p.primary, 20, Ui.withAlpha(p.onPrimary, 0x33)));
        fab.setElevation(Ui.dp(this, 8));
        fabIcon.setColorFilter(p.onPrimary);
        fabLabel.setTextColor(p.onPrimary);
        fabLabel.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        emptyIcon.setColorFilter(p.primary);
        emptyTitle.setTextColor(p.onSurface);
        emptyTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        emptySub.setTextColor(p.onSurfaceVariant);
        emptyAction.setTextColor(p.onPrimary);
        emptyAction.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        emptyAction.setBackground(Ui.rippleBg(this, p.primary, 999, Ui.withAlpha(p.onPrimary, 0x33)));

        bottomNav.setBackgroundColor(p.surfaceContainer);
        findViewById(R.id.nav_divider).setBackgroundColor(Ui.withAlpha(p.outlineVariant, 0xCC));
        navTasksIcon.setImageResource(R.drawable.ic_tasklist);
        navStatsIcon.setImageResource(R.drawable.ic_chart);
        navSettingsIcon.setImageResource(R.drawable.ic_tune);
        navTasks.setContentDescription(getString(R.string.nav_tasks));
        navStats.setContentDescription(getString(R.string.nav_stats));
        navSettings.setContentDescription(getString(R.string.nav_settings));

        statsTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        statsTitle.setTextColor(p.onSurface);
        statsSubtitle.setTextColor(p.onSurfaceVariant);
        statsHero.setBackground(Ui.shape(this, p.primaryContainer, 30));
        statsHero.setPadding(Ui.dp(this, 20), Ui.dp(this, 20),
                Ui.dp(this, 20), Ui.dp(this, 16));
        statsRing.setColors(p.primary, Ui.withAlpha(p.onPrimaryContainer, 0x3A),
                p.onPrimaryContainer);
        statsMotivation.setTextColor(p.onPrimaryContainer);
        statsMotivation.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        statsDoneValue.setTextColor(p.onPrimaryContainer);
        statsPendingValue.setTextColor(p.onPrimaryContainer);
        statsOverdueValue.setTextColor(p.onPrimaryContainer);
        statsDoneValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        statsPendingValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        statsOverdueValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        ((TextView) findViewById(R.id.stats_done_label)).setTextColor(
                Ui.withAlpha(p.onPrimaryContainer, 0xCC));
        ((TextView) findViewById(R.id.stats_pending_label)).setTextColor(
                Ui.withAlpha(p.onPrimaryContainer, 0xCC));
        ((TextView) findViewById(R.id.stats_overdue_label)).setTextColor(
                Ui.withAlpha(p.onPrimaryContainer, 0xCC));

        styleCard(chartCard);
        styleCard(priorityCard);
        styleCard(categoryCard);
        chartTitle.setTextColor(p.onSurface);
        priorityCardTitle.setTextColor(p.onSurface);
        categoryCardTitle.setTextColor(p.onSurface);
        chartTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        priorityCardTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        categoryCardTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        settingsTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        settingsTitle.setTextColor(p.onSurface);
        settingsSubtitle.setTextColor(p.onSurfaceVariant);
    }

    private void styleCard(View card) {
        card.setBackground(Ui.shapeStroke(this, p.surfaceContainerLow, 24,
                Ui.withAlpha(p.outlineVariant, 0xCC), 1));
        card.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
    }

    private void applyInsets() {
        int status = Theme.statusBarHeight(this);
        tasksContent.setPadding(Ui.dp(this, 20), status + Ui.dp(this, 10),
                Ui.dp(this, 20), Ui.dp(this, 116));
        statsContent.setPadding(Ui.dp(this, 20), status + Ui.dp(this, 10),
                Ui.dp(this, 20), Ui.dp(this, 32));
        settingsContent.setPadding(Ui.dp(this, 20), status + Ui.dp(this, 10),
                Ui.dp(this, 20), Ui.dp(this, 32));
    }

    // ------------------------------------------------------------------ listeners / nav

    private void setListeners() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                renderTasks(Store.load(MainActivity.this), false);
            }
        });

        navTasks.setOnClickListener(v -> {
            if (currentPage == 0) {
                tasksScroll.smoothScrollTo(0, 0);
            } else {
                selectPage(0, true);
            }
        });
        navStats.setOnClickListener(v -> selectPage(1, true));
        navSettings.setOnClickListener(v -> selectPage(2, true));

        fab.setOnClickListener(v -> openEditor(-1));
        fab.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });

        emptyAction.setOnClickListener(v -> {
            String q = searchInput.getText().toString().trim();
            if (!q.isEmpty() || filter != FILTER_ALL) {
                searchInput.setText("");
                filter = FILTER_ALL;
                updateChipStyles();
                renderTasks(Store.load(this), false);
            } else {
                openEditor(-1);
            }
        });
    }

    private void selectPage(int index, boolean animate) {
        currentPage = index;
        pageTasks.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        pageStats.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        pageSettings.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        View target = index == 0 ? pageTasks : index == 1 ? pageStats : pageSettings;
        if (animate) {
            target.setAlpha(0f);
            target.setTranslationY(Ui.dp(this, 10));
            target.animate().alpha(1f).translationY(0f).setDuration(220).start();
        } else {
            target.setAlpha(1f);
            target.setTranslationY(0f);
        }

        if (index == 1) refreshStats(Store.load(this));
        if (index == 2) buildSettings();
        applyNavStyles();
    }

    private void applyNavStyles() {
        styleNavItem(0, navTasks, navTasksPill, navTasksIcon, navTasksLabel);
        styleNavItem(1, navStats, navStatsPill, navStatsIcon, navStatsLabel);
        styleNavItem(2, navSettings, navSettingsPill, navSettingsIcon, navSettingsLabel);
    }

    private void styleNavItem(int index, LinearLayout item, FrameLayout pill,
                              ImageView icon, TextView label) {
        boolean active = currentPage == index;
        pill.setBackground(active ? Ui.shape(this, p.primaryContainer, 999)
                : Ui.shape(this, Color.TRANSPARENT, 999));
        icon.setColorFilter(active ? p.onPrimaryContainer : p.onSurfaceVariant);
        label.setTextColor(active ? p.primary : p.onSurfaceVariant);
        label.setTypeface(Typeface.create(
                active ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        item.setSelected(active);
    }

    private void animateEntrance() {
        if (entranceDone || currentPage != 0) return;
        entranceDone = true;
        Ui.animateIn(greeting, 0);
        Ui.animateIn(tasksTitle, 40);
        Ui.animateIn(tasksSubtitle, 70);
        Ui.animateIn(progressCard, 120);
        Ui.animateIn(searchInput, 170);
        Ui.animateIn(fab, 230);
    }

    // ------------------------------------------------------------------ chips

    private void buildFilterChips() {
        filterChips.removeAllViews();
        chipViews.clear();
        for (int i = 0; i < FILTER_LABELS.length; i++) {
            final int index = i;
            TextView chip = Ui.chip(this, FILTER_LABELS[i], p);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(Ui.dp(this, 8));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                filter = index;
                updateChipStyles();
                renderTasks(Store.load(this), false);
                tasksScroll.smoothScrollTo(0, 0);
            });
            filterChips.addView(chip);
            chipViews.add(chip);
        }
        updateChipStyles();
    }

    private void updateChipStyles() {
        for (int i = 0; i < chipViews.size(); i++) {
            Ui.styleChip(chipViews.get(i), i == filter, p);
        }
    }

    // ------------------------------------------------------------------ data refresh

    private void refreshAll(boolean animate) {
        List<Task> all = Store.load(this);
        updateHeader(all);
        updateProgress(all, animate);
        renderTasks(all, animate);
        if (currentPage == 1) refreshStats(all);
        if (currentPage == 2) buildSettings();
    }

    private void updateHeader(List<Task> all) {
        String name = Prefs.name(this);
        greeting.setText(name.isEmpty() ? "Olá 👋" : "Olá, " + name + " 👋");
        int pending = 0;
        for (Task t : all) if (!t.done) pending++;
        String tail = pending == 0 ? "tudo em dia"
                : pending == 1 ? "1 tarefa pendente" : pending + " tarefas pendentes";
        tasksSubtitle.setText(Dates.fullDate(System.currentTimeMillis()) + " • " + tail);
    }

    private void updateProgress(List<Task> all, boolean animate) {
        int total = all.size();
        int done = 0;
        int today = 0;
        int overdue = 0;
        int high = 0;
        long now = System.currentTimeMillis();
        for (Task t : all) {
            if (t.done) done++;
            if (!t.done) {
                if (t.priority == 3) high++;
                if (t.dueAt > 0) {
                    int d = Dates.dayDiff(t.dueAt, now);
                    if (d == 0) today++;
                    if (d < 0) overdue++;
                }
            }
        }
        float pct = total == 0 ? 0f : (float) done / total;
        progressRing.setProgress(pct, animate);

        if (total == 0) {
            progressTitle.setText("Vamos começar");
            progressSub.setText("Crie sua primeira tarefa");
        } else if (done == total) {
            progressTitle.setText("Tudo concluído! 🎉");
            progressSub.setText(done + " de " + total + " tarefas concluídas");
        } else if (pct >= 0.75f) {
            progressTitle.setText("Quase lá");
            progressSub.setText(done + " de " + total + " tarefas concluídas");
        } else if (pct >= 0.35f) {
            progressTitle.setText("Bom ritmo");
            progressSub.setText(done + " de " + total + " tarefas concluídas");
        } else {
            progressTitle.setText("Um passo de cada vez");
            progressSub.setText(done + " de " + total + " tarefas concluídas");
        }

        statTodayValue.setText(String.valueOf(today));
        statOverdueValue.setText(String.valueOf(overdue));
        statHighValue.setText(String.valueOf(high));
    }

    private void renderTasks(List<Task> all, boolean animate) {
        List<Task> shown = filterTasks(all);
        tasksList.removeAllViews();

        if (shown.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            boolean noTasksAtAll = all.isEmpty();
            if (noTasksAtAll) {
                emptyTitle.setText("Tudo em ordem");
                emptySub.setText("Crie sua primeira tarefa e comece a organizar o seu dia.");
                emptyAction.setText("Nova tarefa");
            } else {
                emptyTitle.setText("Nada encontrado");
                emptySub.setText("Tente outro filtro ou um termo de busca diferente.");
                emptyAction.setText("Limpar filtros");
            }
            return;
        }
        emptyState.setVisibility(View.GONE);

        String[] order = {"Atrasadas", "Hoje", "Amanhã", "Esta semana", "Mais tarde",
                "Sem prazo", "Concluídas"};
        LinkedHashMap<String, List<Task>> groups = new LinkedHashMap<>();
        for (String key : order) groups.put(key, new ArrayList<>());
        for (Task t : shown) {
            String key = sectionFor(t);
            List<Task> bucket = groups.get(key);
            if (bucket != null) bucket.add(t);
        }

        int animIndex = 0;
        for (Map.Entry<String, List<Task>> entry : groups.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            View header = sectionHeader(entry.getKey(), entry.getValue().size());
            tasksList.addView(header);
            if (animate && animIndex < 14) Ui.animateIn(header, animIndex * 18L);
            animIndex++;
            for (Task t : entry.getValue()) {
                View card = taskCard(t);
                tasksList.addView(card);
                if (animate && animIndex < 14) Ui.animateIn(card, animIndex * 18L);
                animIndex++;
            }
        }
    }

    private List<Task> filterTasks(List<Task> all) {
        List<Task> out = new ArrayList<>();
        long now = System.currentTimeMillis();
        String q = searchInput.getText().toString().trim().toLowerCase(Dates.PT);
        for (Task t : all) {
            boolean match;
            switch (filter) {
                case FILTER_TODAY:
                    match = !t.done && t.dueAt > 0 && Dates.dayDiff(t.dueAt, now) == 0;
                    break;
                case FILTER_WEEK:
                    match = !t.done && t.dueAt > 0
                            && Dates.dayDiff(t.dueAt, now) >= 0
                            && Dates.dayDiff(t.dueAt, now) <= 7;
                    break;
                case FILTER_OVERDUE:
                    match = !t.done && t.dueAt > 0 && Dates.dayDiff(t.dueAt, now) < 0;
                    break;
                case FILTER_DONE:
                    match = t.done;
                    break;
                default:
                    match = !t.done || Prefs.showCompleted(this);
                    break;
            }
            if (!match) continue;
            if (!q.isEmpty()) {
                String hay = (t.title + " " + t.notes + " " + t.category).toLowerCase(Dates.PT);
                if (!hay.contains(q)) continue;
            }
            out.add(t);
        }
        out.sort(taskComparator);
        return out;
    }

    private static final Comparator<Task> taskComparator = (a, b) -> {
        if (a.done != b.done) return a.done ? 1 : -1;
        if (a.done && b.done) return Long.compare(b.completedAt, a.completedAt);
        if (a.dueAt != b.dueAt) {
            if (a.dueAt == 0) return 1;
            if (b.dueAt == 0) return -1;
            return Long.compare(a.dueAt, b.dueAt);
        }
        if (a.priority != b.priority) return b.priority - a.priority;
        return Long.compare(b.createdAt, a.createdAt);
    };

    private String sectionFor(Task t) {
        if (t.done) return "Concluídas";
        if (t.dueAt <= 0) return "Sem prazo";
        int d = Dates.dayDiff(t.dueAt, System.currentTimeMillis());
        if (d < 0) return "Atrasadas";
        if (d == 0) return "Hoje";
        if (d == 1) return "Amanhã";
        if (d <= 7) return "Esta semana";
        return "Mais tarde";
    }

    private View sectionHeader(String label, int count) {
        LinearLayout header = Ui.row(this);
        header.setPadding(Ui.dp(this, 2), Ui.dp(this, 16), Ui.dp(this, 2), Ui.dp(this, 7));
        TextView title = Ui.text(this, label, 13f, p.primary, true);
        header.addView(title);
        TextView counter = Ui.text(this, "· " + count, 12f, p.onSurfaceVariant, false);
        LinearLayout.LayoutParams counterLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        counterLp.setMarginStart(Ui.dp(this, 7));
        header.addView(counter, counterLp);
        View line = new View(this);
        line.setBackgroundColor(Ui.withAlpha(p.outlineVariant, 0x99));
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(0, 1, 1f);
        lineLp.setMarginStart(Ui.dp(this, 10));
        header.addView(line, lineLp);
        return header;
    }

    private View taskCard(Task t) {
        LinearLayout card = Ui.row(this);
        card.setPadding(Ui.dp(this, 13), Ui.dp(this, 13), Ui.dp(this, 7), Ui.dp(this, 13));
        int bgColor = t.done ? p.surfaceContainer : p.surfaceContainerLow;
        card.setBackground(Ui.rippleBgStroke(this, bgColor, 22,
                Ui.withAlpha(p.outlineVariant, 0xCC), 1,
                Ui.withAlpha(p.onSurface, 0x1F)));
        card.setClickable(true);
        card.setFocusable(true);

        FrameLayout box = new FrameLayout(this);
        int size = Ui.dp(this, 28);
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(size, size);
        boxLp.setMarginEnd(Ui.dp(this, 12));
        box.setLayoutParams(boxLp);
        paintCheckbox(box, t.done);
        ImageView check = new ImageView(this);
        check.setImageResource(R.drawable.ic_check);
        check.setColorFilter(p.onPrimary);
        check.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        check.setPadding(Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6));
        check.setVisibility(t.done ? View.VISIBLE : View.INVISIBLE);
        check.setScaleX(t.done ? 1f : 0.4f);
        check.setScaleY(t.done ? 1f : 0.4f);
        box.addView(check, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        box.setContentDescription(t.done ? "Marcar como pendente" : "Concluir tarefa");
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(v -> toggleDone(t, box, check));
        card.addView(box);

        LinearLayout content = Ui.column(this);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        card.addView(content, contentLp);

        TextView title = Ui.text(this, t.title, 15f,
                t.done ? Ui.withAlpha(p.onSurfaceVariant, 0xCC) : p.onSurface, true);
        title.setMaxLines(2);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        if (t.done) title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        content.addView(title);

        if (!t.notes.isEmpty()) {
            TextView notes = Ui.text(this, t.notes.replace('\n', ' '), 12f,
                    Ui.withAlpha(p.onSurfaceVariant, t.done ? 0x88 : 0xDD), false);
            notes.setMaxLines(1);
            notes.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams notesLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            notesLp.topMargin = Ui.dp(this, 2);
            content.addView(notes, notesLp);
        }

        LinearLayout meta = Ui.row(this);
        LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaLp.topMargin = Ui.dp(this, 6);
        boolean hasMeta = false;

        if (!t.category.isEmpty()) {
            int[] cc = Ui.categoryColors(p, t.category);
            TextView cat = Ui.text(this, t.category, 10.5f, cc[1], true);
            cat.setPadding(Ui.dp(this, 8), Ui.dp(this, 3), Ui.dp(this, 8), Ui.dp(this, 3));
            cat.setBackground(Ui.shape(this, cc[0], 999));
            LinearLayout.LayoutParams catLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            catLp.setMarginEnd(Ui.dp(this, 8));
            meta.addView(cat, catLp);
            hasMeta = true;
        }
        if (t.dueAt > 0) {
            int tint = Dates.isOverdue(t) ? p.error : p.onSurfaceVariant;
            LinearLayout due = Ui.row(this);
            ImageView icon = Ui.image(this, R.drawable.ic_calendar, tint, 13);
            due.addView(icon);
            TextView label = Ui.text(this, Dates.dueLabel(t), 11.5f, tint, false);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.setMarginStart(Ui.dp(this, 4));
            due.addView(label, labelLp);
            LinearLayout.LayoutParams dueLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (hasMeta) dueLp.setMarginStart(Ui.dp(this, 10));
            meta.addView(due, dueLp);
            hasMeta = true;
        }
        if (t.priority > 0) {
            int tint = priorityColor(t.priority);
            LinearLayout pri = Ui.row(this);
            pri.addView(Ui.image(this, R.drawable.ic_flag, tint, 13));
            TextView label = Ui.text(this, priorityLabel(t.priority), 11.5f, tint, false);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.setMarginStart(Ui.dp(this, 4));
            pri.addView(label, labelLp);
            LinearLayout.LayoutParams priLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (hasMeta) priLp.setMarginStart(Ui.dp(this, 10));
            meta.addView(pri, priLp);
            hasMeta = true;
        }
        if (t.repeat > 0) {
            LinearLayout rep = Ui.row(this);
            rep.addView(Ui.image(this, R.drawable.ic_repeat, p.primary, 13));
            TextView label = Ui.text(this, Dates.repeatLabel(t.repeat), 11.5f, p.primary, true);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.setMarginStart(Ui.dp(this, 4));
            rep.addView(label, labelLp);
            LinearLayout.LayoutParams repLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (hasMeta) repLp.setMarginStart(Ui.dp(this, 10));
            meta.addView(rep, repLp);
            hasMeta = true;
        }
        if (hasMeta) content.addView(meta, metaLp);

        ImageButton more = new ImageButton(this);
        more.setImageResource(R.drawable.ic_more);
        more.setColorFilter(p.onSurfaceVariant);
        more.setBackground(Ui.rippleCircle(this, Color.TRANSPARENT,
                Ui.withAlpha(p.onSurfaceVariant, 0x33)));
        more.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        more.setContentDescription("Mais opções");
        LinearLayout.LayoutParams moreLp = new LinearLayout.LayoutParams(
                Ui.dp(this, 38), Ui.dp(this, 38));
        moreLp.setMarginStart(Ui.dp(this, 2));
        card.addView(more, moreLp);
        more.setOnClickListener(v -> showTaskSheet(t));

        card.setOnClickListener(v -> openEditor(t.id));
        card.setOnLongClickListener(v -> {
            showTaskSheet(t);
            return true;
        });
        return card;
    }

    private void paintCheckbox(FrameLayout box, boolean done) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        if (done) {
            circle.setColor(p.primary);
        } else {
            circle.setColor(Color.TRANSPARENT);
            circle.setStroke(Ui.dp(this, 2), p.outline);
        }
        box.setBackground(circle);
    }

    private void toggleDone(Task t, FrameLayout box, ImageView check) {
        t.done = !t.done;
        t.completedAt = t.done ? System.currentTimeMillis() : 0L;
        persistDone(t);
        box.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

        paintCheckbox(box, t.done);
        if (t.done) {
            check.setVisibility(View.VISIBLE);
            check.setAlpha(0f);
            check.setScaleX(0.4f);
            check.setScaleY(0.4f);
            check.animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(200).start();
        } else {
            check.animate().alpha(0f).scaleX(0.4f).scaleY(0.4f)
                    .setDuration(120).withEndAction(() -> check.setVisibility(View.INVISIBLE)).start();
        }

        List<Task> all = Store.load(this);
        updateHeader(all);
        updateProgress(all, true);
        tasksList.postDelayed(() -> refreshAll(false), 260);
    }

    /**
     * Salva a tarefa e, se ela é recorrente e foi concluída, cria a próxima ocorrência
     * mantendo o horário do prazo. Retorna o horário da próxima ocorrência (0 se não houver).
     */
    private long persistDone(Task t) {
        if (t.done && t.repeat > 0) {
            Task existing = t.nextId > 0 ? Store.find(this, t.nextId) : null;
            if (existing == null) {
                Task next = t.copy();
                next.dueAt = Dates.nextOccurrence(t.dueAt, t.repeat, System.currentTimeMillis());
                next.nextId = 0L;
                t.nextId = next.id;
                Store.upsert(this, t);
                Store.upsert(this, next);
                return next.dueAt;
            }
        }
        Store.upsert(this, t);
        return 0L;
    }

    private String priorityLabel(int priority) {
        switch (priority) {
            case 3:
                return "Alta";
            case 2:
                return "Média";
            case 1:
                return "Baixa";
            default:
                return "";
        }
    }

    private int priorityColor(int priority) {
        switch (priority) {
            case 3:
                return p.error;
            case 2:
                return p.tertiary;
            case 1:
                return p.secondary;
            default:
                return p.onSurfaceVariant;
        }
    }

    // ------------------------------------------------------------------ task actions

    private void showTaskSheet(Task t) {
        StringBuilder sub = new StringBuilder();
        if (!t.category.isEmpty()) sub.append(t.category);
        if (t.dueAt > 0) {
            if (sub.length() > 0) sub.append(" • ");
            sub.append(Dates.dueLabel(t));
        }
        if (t.repeat > 0) {
            if (sub.length() > 0) sub.append(" • ");
            sub.append(Dates.repeatLabel(t.repeat));
        }
        if (sub.length() == 0) sub.append(t.done ? "Concluída" : "Pendente");

        List<Ui.Item> items = new ArrayList<>();
        items.add(new Ui.Item(R.drawable.ic_edit, "Editar", p.onSurface,
                () -> openEditor(t.id)));
        items.add(new Ui.Item(t.done ? R.drawable.ic_undo : R.drawable.ic_check_circle,
                t.done ? "Reabrir tarefa" : "Concluir tarefa", p.primary, () -> {
            Task fresh = Store.find(this, t.id);
            if (fresh != null) {
                fresh.done = !fresh.done;
                fresh.completedAt = fresh.done ? System.currentTimeMillis() : 0L;
                long nextAt = persistDone(fresh);
                refreshAll(false);
                if (nextAt > 0) {
                    String when = Dates.relative(nextAt, System.currentTimeMillis());
                    if (fresh.hasTime) when += ", " + Dates.time(nextAt);
                    Ui.snackbar(this, p, "Próxima ocorrência: " + when, null, null, 84);
                }
            }
        }));
        items.add(new Ui.Item(R.drawable.ic_duplicate, "Duplicar", p.onSurface, () -> {
            Task copy = t.copy();
            Store.upsert(this, copy);
            refreshAll(false);
            Ui.snackbar(this, p, "Tarefa duplicada", null, null, 84);
        }));
        items.add(new Ui.Item(R.drawable.ic_delete, "Excluir", p.error, () -> {
            Store.delete(this, t.id);
            refreshAll(false);
            Ui.snackbar(this, p, "Tarefa excluída", "Desfazer", () -> {
                Store.upsert(this, t);
                refreshAll(false);
            }, 84);
        }));

        Ui.sheet(this, p, t.title, sub.toString(), items);
    }

    private void openEditor(long taskId) {
        Intent i = new Intent(this, EditorActivity.class);
        if (taskId > 0) i.putExtra("task_id", taskId);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
    }

    // ------------------------------------------------------------------ stats

    private void refreshStats(List<Task> all) {
        int total = all.size();
        int done = 0, overdue = 0, pendingHigh = 0;
        long now = System.currentTimeMillis();
        for (Task t : all) {
            if (t.done) done++;
            if (!t.done) {
                if (t.dueAt > 0 && Dates.dayDiff(t.dueAt, now) < 0) overdue++;
                if (t.priority == 3) pendingHigh++;
            }
        }
        int pending = total - done;
        float pct = total == 0 ? 0f : (float) done / total;

        statsRing.setProgress(pct, true);
        statsDoneValue.setText(String.valueOf(done));
        statsPendingValue.setText(String.valueOf(pending));
        statsOverdueValue.setText(String.valueOf(overdue));

        if (total == 0) {
            statsMotivation.setText("Crie e conclua tarefas para ver seu progresso");
        } else if (done == total) {
            statsMotivation.setText("Tudo concluído! 🎉");
        } else if (pct >= 0.7f) {
            statsMotivation.setText("Você está quase lá");
        } else if (pct >= 0.4f) {
            statsMotivation.setText("Bom ritmo, continue assim");
        } else if (done > 0) {
            statsMotivation.setText("Um passo de cada vez");
        } else {
            statsMotivation.setText("Vamos começar?");
        }
        statsSubtitle.setText(pendingHigh > 0
                ? pendingHigh + (pendingHigh == 1
                ? " tarefa de alta prioridade em aberto" : " tarefas de alta prioridade em aberto")
                : "Seu progresso geral");

        int[] values = new int[7];
        String[] labels = new String[7];
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(Dates.startOfDay(now));
        day.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            labels[i] = Dates.chartDay(day.getTimeInMillis());
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
        for (Task t : all) {
            if (!t.done || t.completedAt <= 0) continue;
            int diff = Dates.dayDiff(t.completedAt, now);
            if (diff >= 0 && diff <= 6) values[6 - diff]++;
        }
        chart.setData(values, labels, p, true);

        buildPriorityRows(all);
        buildCategoryRows(all);
    }

    private void buildPriorityRows(List<Task> all) {
        priorityList.removeAllViews();
        int max = 1;
        int[] counts = new int[4];
        for (Task t : all) {
            if (!t.done) {
                int pr = Math.max(0, Math.min(3, t.priority));
                counts[pr]++;
                max = Math.max(max, counts[pr]);
            }
        }
        int[] priorities = {3, 2, 1};
        for (int pr : priorities) {
            LinearLayout row = Ui.row(this);
            row.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));

            View dot = new View(this);
            dot.setBackground(Ui.shape(this, priorityColor(pr), 999));
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(
                    Ui.dp(this, 10), Ui.dp(this, 10));
            row.addView(dot, dotLp);

            TextView label = Ui.text(this, priorityLabel(pr), 12.5f, p.onSurface, false);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    Ui.dp(this, 58), ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.setMarginStart(Ui.dp(this, 8));
            row.addView(label, labelLp);

            MeterView meter = new MeterView(this);
            meter.setColors(priorityColor(pr), Ui.withAlpha(p.outlineVariant, 0x66));
            meter.setValue((float) counts[pr] / max);
            LinearLayout.LayoutParams meterLp = new LinearLayout.LayoutParams(0, Ui.dp(this, 8), 1f);
            meterLp.setMarginStart(Ui.dp(this, 6));
            meterLp.setMarginEnd(Ui.dp(this, 12));
            row.addView(meter, meterLp);

            TextView count = Ui.text(this, String.valueOf(counts[pr]), 12.5f,
                    p.onSurfaceVariant, true);
            count.setGravity(Gravity.END);
            row.addView(count, new LinearLayout.LayoutParams(
                    Ui.dp(this, 24), ViewGroup.LayoutParams.WRAP_CONTENT));
            priorityList.addView(row);
        }
    }

    private void buildCategoryRows(List<Task> all) {
        categoryList.removeAllViews();
        Map<String, Integer> counts = new LinkedHashMap<>();
        int totalCategorized = 0;
        for (Task t : all) {
            if (t.category.isEmpty()) continue;
            counts.put(t.category, counts.getOrDefault(t.category, 0) + 1);
            totalCategorized++;
        }
        if (counts.isEmpty()) {
            TextView empty = Ui.text(this, "Nenhuma categoria ainda",
                    13f, p.onSurfaceVariant, false);
            empty.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            categoryList.addView(empty);
            return;
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        int max = 1;
        for (Map.Entry<String, Integer> e : entries) max = Math.max(max, e.getValue());
        int limit = Math.min(6, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            LinearLayout item = Ui.column(this);
            item.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));

            LinearLayout top = Ui.row(this);
            TextView name = Ui.text(this, e.getKey(), 13f, p.onSurface, false);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            top.addView(name, nameLp);
            TextView count = Ui.text(this, e.getValue() + (e.getValue() == 1 ? " tarefa" : " tarefas"),
                    11.5f, p.onSurfaceVariant, false);
            top.addView(count);
            item.addView(top);

            MeterView meter = new MeterView(this);
            meter.setColors(categoryFill(e.getKey()),
                    Ui.withAlpha(p.outlineVariant, 0x66));
            meter.setValue((float) e.getValue() / max);
            LinearLayout.LayoutParams meterLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 6));
            meterLp.topMargin = Ui.dp(this, 7);
            item.addView(meter, meterLp);
            categoryList.addView(item);
        }
    }

    private int categoryFill(String category) {
        int hash = Math.abs(category.hashCode()) % 3;
        if (hash == 1) return p.secondary;
        if (hash == 2) return p.tertiary;
        return p.primary;
    }

    // ------------------------------------------------------------------ settings

    private void buildSettings() {
        settingsBody.removeAllViews();

        addSectionLabel("Aparência");
        LinearLayout lookCard = newCard();
        boolean dynamicSupported = Build.VERSION.SDK_INT >= 31;
        addSwitchRow(lookCard, R.drawable.ic_palette,
                "Cores dinâmicas",
                dynamicSupported
                        ? "Material You: as cores do seu papel de parede"
                        : "Requer Android 12 ou superior",
                Prefs.dynamicColor(this), dynamicSupported, checked -> {
            Prefs.setDynamicColor(this, checked);
            recreate();
        });
        addPalettePicker(lookCard);
        addThemePicker(lookCard);
        addSwitchRow(lookCard, R.drawable.ic_tasklist,
                "Mostrar concluídas",
                "Exibir tarefas concluídas na lista",
                Prefs.showCompleted(this), true, checked -> {
            Prefs.setShowCompleted(this, checked);
            List<Task> all = Store.load(this);
            updateHeader(all);
            updateProgress(all, true);
            renderTasks(all, false);
            if (currentPage == 1) refreshStats(all);
        });
        settingsBody.addView(lookCard);

        addSectionLabel("Personalização");
        LinearLayout personalCard = newCard();
        String name = Prefs.name(this);
        addActionRow(personalCard, R.drawable.ic_person, "Seu nome",
                name.isEmpty() ? "Adicione um nome para a saudação" : name,
                p.onSurface, p.onSurfaceVariant, true, () ->
                        Ui.prompt(this, p, "Seu nome", "Como podemos te chamar?",
                                Prefs.name(this), true, value -> {
                                    Prefs.setName(this, value);
                                    refreshAll(false);
                                }));
        addActionRow(personalCard, R.drawable.ic_export, "Exportar tarefas",
                "Compartilhar sua lista como texto",
                p.onSurface, p.onSurfaceVariant, false, this::exportTasks);

        int doneCount = 0;
        for (Task t : Store.load(this)) if (t.done) doneCount++;
        final int doneTotal = doneCount;
        addActionRow(personalCard, R.drawable.ic_cleaning, "Limpar concluídas",
                doneCount == 0 ? "Nenhuma tarefa concluída"
                        : doneCount + (doneCount == 1 ? " tarefa concluída" : " tarefas concluídas"),
                p.onSurface, p.onSurfaceVariant, false, () -> {
                    if (doneTotal == 0) {
                        Ui.snackbar(this, p, "Nenhuma tarefa concluída", null, null, 84);
                        return;
                    }
                    Ui.confirm(this, p, "Limpar concluídas?",
                            "As " + doneTotal + " tarefas concluídas serão removidas.",
                            "Limpar", true, () -> {
                                int removed = Store.clearCompleted(this);
                                refreshAll(false);
                                buildSettings();
                                Ui.snackbar(this, p, removed + " tarefas removidas",
                                        null, null, 84);
                            });
                });

        addActionRow(personalCard, R.drawable.ic_delete, "Apagar todas as tarefas",
                "Remover permanentemente toda a lista",
                p.error, p.error, false, () -> Ui.confirm(this, p,
                        "Apagar tudo?",
                        "Todas as tarefas serão removidas. Essa ação não pode ser desfeita.",
                        "Apagar tudo", true, () -> {
                            Store.save(this, new ArrayList<>());
                            refreshAll(false);
                            buildSettings();
                            Ui.snackbar(this, p, "Lista apagada", null, null, 84);
                        }));
        settingsBody.addView(personalCard);

        addSectionLabel("Sobre");
        LinearLayout aboutCard = newCard();
        addActionRow(aboutCard, R.drawable.ic_info, "Fluxo",
                "Versão 1.0 • Material You 3", p.onSurface,
                p.onSurfaceVariant, false, null);
        TextView note = Ui.text(this,
                "As cores acompanham o papel de parede e o tema do sistema. "
                        + "Seus dados ficam apenas neste dispositivo.",
                12f, p.onSurfaceVariant, false);
        note.setLineSpacing(Ui.dp(this, 3), 1f);
        note.setPadding(Ui.dp(this, 16), Ui.dp(this, 4),
                Ui.dp(this, 16), Ui.dp(this, 12));
        aboutCard.addView(note);
        settingsBody.addView(aboutCard);

        settingsSubtitle.setText("Deixe o Fluxo com a sua cara.");
    }

    private LinearLayout newCard() {
        LinearLayout card = Ui.column(this);
        card.setBackground(Ui.shapeStroke(this, p.surfaceContainerLow, 24,
                Ui.withAlpha(p.outlineVariant, 0xCC), 1));
        card.setPadding(Ui.dp(this, 6), Ui.dp(this, 6),
                Ui.dp(this, 6), Ui.dp(this, 6));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(lp);
        return card;
    }

    private void addSectionLabel(String text) {
        TextView label = Ui.label(this, text, 12f, p.primary);
        label.setPadding(Ui.dp(this, 6), Ui.dp(this, 20), Ui.dp(this, 6), Ui.dp(this, 8));
        settingsBody.addView(label);
    }

    private void addSwitchRow(LinearLayout card, int iconRes, String title, String subtitle,
                              boolean checked, boolean enabled,
                              SwitchView.OnCheckedChangeListener listener) {
        LinearLayout row = Ui.row(this);
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        row.setBackground(Ui.rippleBg(this, Color.TRANSPARENT, 18,
                Ui.withAlpha(p.onSurface, 0x16)));
        ImageView icon = Ui.image(this, iconRes, p.onSurfaceVariant, 22);
        row.addView(icon);
        LinearLayout texts = Ui.column(this);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textLp.setMarginStart(Ui.dp(this, 14));
        row.addView(texts, textLp);
        TextView t = Ui.text(this, title, 14.5f, p.onSurface, true);
        texts.addView(t);
        TextView s = Ui.text(this, subtitle, 11.5f, p.onSurfaceVariant, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = Ui.dp(this, 2);
        texts.addView(s, subLp);

        SwitchView sw = new SwitchView(this);
        sw.setPalette(p);
        sw.setCheckedSilently(checked);
        if (!enabled) {
            sw.setEnabled(false);
            sw.setAlpha(0.4f);
            row.setAlpha(0.55f);
        } else {
            sw.setOnCheckedChangeListener(listener);
            row.setOnClickListener(v -> sw.performClick());
        }
        LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(
                Ui.dp(this, 52), Ui.dp(this, 32));
        swLp.setMarginStart(Ui.dp(this, 12));
        row.addView(sw, swLp);
        card.addView(row);
    }

    private void addPalettePicker(LinearLayout card) {
        LinearLayout header = Ui.column(this);
        header.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), 0);
        TextView title = Ui.text(this, "Cores do app", 14.5f, p.onSurface, true);
        header.addView(title);

        int custom = Prefs.customSeed(this);
        boolean dynamic = Prefs.dynamicColor(this) && Build.VERSION.SDK_INT >= 31;
        String subtitle;
        if (dynamic) {
            subtitle = "Automática • segue o papel de parede";
        } else if (custom != 0) {
            subtitle = String.format(Locale.US, "#%06X", 0xFFFFFF & custom)
                    + " • cor personalizada";
        } else {
            subtitle = Palette.SEED_NAMES[Prefs.seedIndex(this)] + " • paleta do app";
        }
        TextView sub = Ui.text(this, subtitle, 11.5f, p.onSurfaceVariant, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = Ui.dp(this, 2);
        header.addView(sub, subLp);
        card.addView(header);

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        LinearLayout swatches = Ui.row(this);
        swatches.setPadding(Ui.dp(this, 16), Ui.dp(this, 10),
                Ui.dp(this, 16), Ui.dp(this, 10));
        for (int i = 0; i < Palette.SEEDS.length; i++) {
            final int index = i;
            boolean selected = !dynamic && custom == 0 && i == Prefs.seedIndex(this);
            FrameLayout holder = new FrameLayout(this);
            holder.setBackground(Ui.shapeStroke(this, Color.TRANSPARENT, 999,
                    selected ? p.primary : Color.TRANSPARENT, 2));
            View circle = new View(this);
            circle.setBackground(Ui.shape(this, Palette.SEEDS[i], 999));
            FrameLayout.LayoutParams circleLp = new FrameLayout.LayoutParams(
                    Ui.dp(this, 36), Ui.dp(this, 36), Gravity.CENTER);
            holder.addView(circle, circleLp);
            if (selected) {
                ImageView check = new ImageView(this);
                check.setImageResource(R.drawable.ic_check);
                check.setColorFilter(Ui.isDark(Palette.SEEDS[i]) ? Color.WHITE : Color.BLACK);
                check.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                check.setPadding(Ui.dp(this, 7), Ui.dp(this, 7),
                        Ui.dp(this, 7), Ui.dp(this, 7));
                holder.addView(check, new FrameLayout.LayoutParams(
                        Ui.dp(this, 28), Ui.dp(this, 28), Gravity.CENTER));
            }
            holder.setContentDescription(Palette.SEED_NAMES[i]);
            holder.setClickable(true);
            holder.setFocusable(true);
            holder.setOnClickListener(v -> {
                Prefs.setDynamicColor(this, false);
                Prefs.setCustomSeed(this, 0);
                Prefs.setSeedIndex(this, index);
                recreate();
            });
            LinearLayout.LayoutParams holderLp = new LinearLayout.LayoutParams(
                    Ui.dp(this, 46), Ui.dp(this, 46));
            holderLp.setMarginEnd(Ui.dp(this, 4));
            swatches.addView(holder, holderLp);
        }

        // Amostra personalizada: gradiente livre
        boolean customSelected = !dynamic && custom != 0;
        FrameLayout customHolder = new FrameLayout(this);
        customHolder.setBackground(Ui.shapeStroke(this, Color.TRANSPARENT, 999,
                customSelected ? p.primary : Color.TRANSPARENT, 2));
        View rainbow = new View(this);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF43A047,
                        0xFF1E88E5, 0xFF8E24AA, 0xFFE53935});
        gradient.setShape(GradientDrawable.OVAL);
        rainbow.setBackground(gradient);
        customHolder.addView(rainbow, new FrameLayout.LayoutParams(
                Ui.dp(this, 36), Ui.dp(this, 36), Gravity.CENTER));
        if (customSelected) {
            ImageView check = new ImageView(this);
            check.setImageResource(R.drawable.ic_check);
            check.setColorFilter(Color.WHITE);
            check.setBackground(Ui.shape(this, 0x66000000, 999));
            check.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            check.setPadding(Ui.dp(this, 6), Ui.dp(this, 6),
                    Ui.dp(this, 6), Ui.dp(this, 6));
            customHolder.addView(check, new FrameLayout.LayoutParams(
                    Ui.dp(this, 26), Ui.dp(this, 26), Gravity.CENTER));
        } else {
            ImageView plus = new ImageView(this);
            plus.setImageResource(R.drawable.ic_add);
            plus.setColorFilter(Color.WHITE);
            plus.setBackground(Ui.shape(this, 0x66000000, 999));
            plus.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            plus.setPadding(Ui.dp(this, 6), Ui.dp(this, 6),
                    Ui.dp(this, 6), Ui.dp(this, 6));
            customHolder.addView(plus, new FrameLayout.LayoutParams(
                    Ui.dp(this, 26), Ui.dp(this, 26), Gravity.CENTER));
        }
        customHolder.setContentDescription("Escolher uma cor personalizada");
        customHolder.setClickable(true);
        customHolder.setFocusable(true);
        customHolder.setOnClickListener(v -> openColorPicker());
        LinearLayout.LayoutParams customLp = new LinearLayout.LayoutParams(
                Ui.dp(this, 46), Ui.dp(this, 46));
        customLp.setMarginEnd(Ui.dp(this, 4));
        swatches.addView(customHolder, customLp);

        scroll.addView(swatches);
        card.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 62)));

        String customSubtitle;
        if (dynamic) {
            customSubtitle = "Escolha qualquer cor (desativa o modo automático)";
        } else if (custom != 0) {
            customSubtitle = "Cor atual " + String.format(Locale.US, "#%06X", 0xFFFFFF & custom);
        } else {
            customSubtitle = "Escolha qualquer cor para o app";
        }
        addActionRow(card, R.drawable.ic_palette, "Personalizar cor",
                customSubtitle, p.onSurface, p.primary, true, this::openColorPicker);
    }

    private void openColorPicker() {
        int current = Prefs.customSeed(this);
        if (current == 0) {
            current = Prefs.dynamicColor(this) && Build.VERSION.SDK_INT >= 31
                    ? p.seed
                    : Palette.SEEDS[Prefs.seedIndex(this)];
        }
        Ui.colorPicker(this, p, current, color -> {
            Prefs.setDynamicColor(this, false);
            Prefs.setCustomSeed(this, color);
            recreate();
        });
    }

    private void addThemePicker(LinearLayout card) {
        LinearLayout wrap = Ui.column(this);
        wrap.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 14));
        TextView title = Ui.text(this, "Tema", 14.5f, p.onSurface, true);
        wrap.addView(title);

        LinearLayout seg = Ui.row(this);
        seg.setBackground(Ui.shape(this, p.surfaceContainerHighest, 999));
        seg.setPadding(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        LinearLayout.LayoutParams segLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        segLp.topMargin = Ui.dp(this, 10);
        String[] labels = {"Sistema", "Claro", "Escuro"};
        int mode = Prefs.themeMode(this);
        for (int i = 0; i < 3; i++) {
            final int index = i;
            TextView option = Ui.text(this, labels[i], 12.5f,
                    i == mode ? p.onSecondaryContainer : p.onSurfaceVariant, true);
            option.setGravity(Gravity.CENTER);
            option.setPadding(0, Ui.dp(this, 9), 0, Ui.dp(this, 9));
            if (i == mode) {
                option.setBackground(Ui.shape(this, p.secondaryContainer, 999));
            }
            option.setClickable(true);
            option.setFocusable(true);
            option.setOnClickListener(v -> {
                if (Prefs.themeMode(this) != index) {
                    Prefs.setThemeMode(this, index);
                    recreate();
                }
            });
            seg.addView(option, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        wrap.addView(seg, segLp);
        card.addView(wrap);
    }

    private void addActionRow(LinearLayout card, int iconRes, String title, String subtitle,
                              int titleColor, int iconColor, boolean chevron, Runnable action) {
        LinearLayout row = Ui.row(this);
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        row.setBackground(Ui.rippleBg(this, Color.TRANSPARENT, 18,
                Ui.withAlpha(p.onSurface, 0x16)));
        row.addView(Ui.image(this, iconRes, iconColor, 22));
        LinearLayout texts = Ui.column(this);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textLp.setMarginStart(Ui.dp(this, 14));
        row.addView(texts, textLp);
        texts.addView(Ui.text(this, title, 14.5f, titleColor, true));
        TextView sub = Ui.text(this, subtitle, 11.5f, p.onSurfaceVariant, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = Ui.dp(this, 2);
        texts.addView(sub, subLp);
        if (chevron) {
            row.addView(Ui.image(this, R.drawable.ic_chevron, p.onSurfaceVariant, 20));
        }
        if (action != null) {
            row.setOnClickListener(v -> action.run());
        }
        card.addView(row);
    }

    private void exportTasks() {
        List<Task> all = Store.load(this);
        StringBuilder sb = new StringBuilder();
        sb.append("Fluxo — Minhas tarefas\n");
        sb.append(Dates.fullDate(System.currentTimeMillis())).append("\n\n");
        if (all.isEmpty()) {
            sb.append("Nenhuma tarefa por aqui.\n");
        } else {
            for (Task t : all) {
                sb.append(t.done ? "[x] " : "[ ] ");
                if (t.priority > 0) sb.append("(").append(priorityLabel(t.priority)).append(") ");
                sb.append(t.title);
                if (!t.category.isEmpty()) sb.append(" — ").append(t.category);
                if (t.dueAt > 0) sb.append(" — ").append(Dates.dueLabel(t));
                sb.append("\n");
            }
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Minhas tarefas — Fluxo");
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(send, "Exportar tarefas"));
    }
}
