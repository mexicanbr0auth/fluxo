package com.fluxo.tarefas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fluxo.tarefas.data.Store;
import com.fluxo.tarefas.model.Task;
import com.fluxo.tarefas.reminder.ReminderScheduler;
import com.fluxo.tarefas.ui.Dates;
import com.fluxo.tarefas.ui.Palette;
import com.fluxo.tarefas.ui.Theme;
import com.fluxo.tarefas.ui.Ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class EditorActivity extends android.app.Activity {

    private Palette p;
    private Task task;
    private boolean isNew;
    private boolean saved = false;

    private long dueAt = 0L;
    private boolean hasTime = false;
    private int priority = 0;
    private String category = "";
    private int repeat = 0;
    private int weekdays = 0;

    private String initialTitle = "";
    private String initialNotes = "";
    private String initialCategory = "";
    private int initialPriority = 0;
    private long initialDue = 0L;
    private boolean initialHasTime = false;
    private int initialRepeat = 0;
    private int initialWeekdays = 0;

    private LinearLayout topbar, content, categoryChips, priorityChips, dateChips,
            timeChips, repeatChips, weekdayChips;
    private ImageButton btnClose;
    private TextView editorTitle, btnSave, btnDelete, dueSummary,
            labelCategory, labelPriority, labelDue, labelRepeat;
    private EditText etTitle, etNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean dark = Theme.isDark(this);
        setTheme(dark ? R.style.Theme_Fluxo_Dark : R.style.Theme_Fluxo);
        super.onCreate(savedInstanceState);
        p = Theme.palette(this);
        Theme.setupWindow(this, p);
        setContentView(R.layout.activity_editor);

        bindViews();

        long id = getIntent().getLongExtra("task_id", -1);
        if (id > 0) {
            task = Store.find(this, id);
            if (task == null) {
                finish();
                return;
            }
            isNew = false;
            dueAt = task.dueAt;
            hasTime = task.hasTime;
            priority = task.priority;
            category = task.category;
            repeat = task.repeat;
            weekdays = task.weekdays;
        } else {
            isNew = true;
        }

        if (savedInstanceState != null) {
            dueAt = savedInstanceState.getLong("dueAt", dueAt);
            hasTime = savedInstanceState.getBoolean("hasTime", hasTime);
            priority = savedInstanceState.getInt("priority", priority);
            category = savedInstanceState.getString("category", category);
            repeat = savedInstanceState.getInt("repeat", repeat);
            weekdays = savedInstanceState.getInt("weekdays", weekdays);
        }

        roleSetup();
        applyTheme();
        applyInsets();

        if (task != null) {
            etTitle.setText(task.title);
            etNotes.setText(task.notes);
            etTitle.setSelection(etTitle.getText().length());
        }

        initialTitle = etTitle.getText().toString();
        initialNotes = etNotes.getText().toString();
        initialCategory = category;
        initialPriority = priority;
        initialDue = dueAt;
        initialHasTime = hasTime;
        initialRepeat = repeat;
        initialWeekdays = weekdays;

        buildCategoryChips();
        buildPriorityChips();
        refreshDueUi();
        setListeners();
    }

    private void bindViews() {
        topbar = findViewById(R.id.editor_topbar);
        content = findViewById(R.id.editor_content);
        categoryChips = findViewById(R.id.category_chips);
        priorityChips = findViewById(R.id.priority_chips);
        dateChips = findViewById(R.id.date_chips);
        timeChips = findViewById(R.id.time_chips);
        repeatChips = findViewById(R.id.repeat_chips);
        weekdayChips = findViewById(R.id.weekday_chips);
        btnClose = findViewById(R.id.btn_close);
        editorTitle = findViewById(R.id.editor_title);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        dueSummary = findViewById(R.id.due_summary);
        labelCategory = findViewById(R.id.label_category);
        labelPriority = findViewById(R.id.label_priority);
        labelDue = findViewById(R.id.label_due);
        labelRepeat = findViewById(R.id.label_repeat);
        etTitle = findViewById(R.id.et_title);
        etNotes = findViewById(R.id.et_notes);
    }

    private void roleSetup() {
        editorTitle.setText(isNew ? R.string.editor_new : R.string.editor_edit);
        btnDelete.setVisibility(isNew ? View.GONE : View.VISIBLE);
    }

    private void applyTheme() {
        findViewById(R.id.editor_root).setBackgroundColor(p.surface);
        topbar.setBackgroundColor(p.surface);

        btnClose.setColorFilter(p.onSurface);
        btnClose.setBackground(Ui.rippleCircle(this, Color.TRANSPARENT,
                Ui.withAlpha(p.onSurface, 0x33)));
        editorTitle.setTextColor(p.onSurface);
        editorTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        btnSave.setTextColor(p.onPrimary);
        btnSave.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        btnSave.setBackground(Ui.rippleBg(this, p.primary, 999,
                Ui.withAlpha(p.onPrimary, 0x33)));

        etTitle.setTextColor(p.onSurface);
        etTitle.setHintTextColor(Ui.withAlpha(p.onSurfaceVariant, 0xCC));
        etTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        etNotes.setTextColor(p.onSurfaceVariant);
        etNotes.setHintTextColor(Ui.withAlpha(p.onSurfaceVariant, 0xAA));

        findViewById(R.id.editor_divider_1).setBackgroundColor(
                Ui.withAlpha(p.outlineVariant, 0xCC));
        labelCategory.setTextColor(p.onSurfaceVariant);
        labelPriority.setTextColor(p.onSurfaceVariant);
        labelDue.setTextColor(p.onSurfaceVariant);
        labelRepeat.setTextColor(p.onSurfaceVariant);
        labelCategory.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        labelPriority.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        labelDue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        labelRepeat.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        dueSummary.setTextColor(p.onSurfaceVariant);

        btnDelete.setTextColor(p.error);
        btnDelete.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        Drawable del = getDrawable(R.drawable.ic_delete);
        if (del != null) {
            del.setTint(p.error);
            btnDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(del, null, null, null);
            btnDelete.setCompoundDrawablePadding(Ui.dp(this, 8));
            btnDelete.setCompoundDrawableTintList(ColorStateList.valueOf(p.error));
        }
    }

    private void applyInsets() {
        int status = Theme.statusBarHeight(this);
        topbar.setPadding(Ui.dp(this, 8), status + Ui.dp(this, 6),
                Ui.dp(this, 14), Ui.dp(this, 6));
        content.setPadding(Ui.dp(this, 20), Ui.dp(this, 8),
                Ui.dp(this, 20), Ui.dp(this, 44));
    }

    private void setListeners() {
        btnClose.setOnClickListener(v -> handleClose());
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> Ui.confirm(this, p, "Excluir tarefa?",
                "Essa ação não pode ser desfeita.", "Excluir", true, () -> {
                    if (task != null) {
                        Store.delete(this, task.id);
                        ReminderScheduler.cancel(this, task.id);
                    }
                    saved = true;
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
                }));

        if (isNew) {
            etTitle.requestFocus();
            Ui.showKeyboard(etTitle);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    // --------------------------------------------------------------- chips

    private void addChip(LinearLayout parent, String label, boolean selected, Runnable action) {
        TextView chip = Ui.chip(this, label, p);
        if (selected) {
            chip.setTextColor(p.onSecondaryContainer);
            chip.setBackground(Ui.rippleBg(this, p.secondaryContainer, 999,
                    Ui.withAlpha(p.onSecondaryContainer, 0x33)));
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(Ui.dp(this, 8));
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> action.run());
        parent.addView(chip);
    }

    private void buildCategoryChips() {
        categoryChips.removeAllViews();
        List<String> cats = new ArrayList<>(Arrays.asList(
                "Pessoal", "Trabalho", "Estudos", "Saúde", "Casa", "Compras"));
        for (String c : Store.categories(this)) {
            if (!cats.contains(c)) cats.add(c);
        }
        if (!category.isEmpty() && !cats.contains(category)) cats.add(0, category);

        addChip(categoryChips, "Nenhuma", category.isEmpty(), () -> {
            category = "";
            buildCategoryChips();
        });
        for (String c : cats) {
            addChip(categoryChips, c, c.equals(category), () -> {
                category = c;
                buildCategoryChips();
            });
        }
        addChip(categoryChips, "+ Nova", false, () -> Ui.prompt(this, p,
                "Nova categoria", "Nome da categoria", "", value -> {
                    category = value;
                    buildCategoryChips();
                }));
    }

    private void buildPriorityChips() {
        priorityChips.removeAllViews();
        int[] values = {0, 1, 2, 3};
        String[] labels = {"Nenhuma", "Baixa", "Média", "Alta"};
        for (int i = 0; i < values.length; i++) {
            final int value = values[i];
            boolean selected = priority == value;

            SpannableString text = new SpannableString("●  " + labels[i]);
            text.setSpan(new ForegroundColorSpan(priorityColor(value)),
                    0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView chip = Ui.text(this, "", 13f, p.onSurfaceVariant, true);
            chip.setText(text);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(Ui.dp(this, 16), Ui.dp(this, 9), Ui.dp(this, 16), Ui.dp(this, 9));
            chip.setMinHeight(Ui.dp(this, 36));
            if (selected) {
                chip.setTextColor(priorityOnContainer(value));
                chip.setBackground(Ui.rippleBg(this, priorityContainer(value), 999,
                        Ui.withAlpha(priorityOnContainer(value), 0x33)));
            } else {
                chip.setTextColor(p.onSurfaceVariant);
                chip.setBackground(Ui.rippleBgStroke(this, Color.TRANSPARENT, 999,
                        p.outline, 1, Ui.withAlpha(p.onSurfaceVariant, 0x22)));
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(Ui.dp(this, 8));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                priority = value;
                buildPriorityChips();
            });
            priorityChips.addView(chip);
        }
    }

    private int priorityColor(int value) {
        switch (value) {
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

    private int priorityContainer(int value) {
        switch (value) {
            case 3:
                return p.errorContainer;
            case 2:
                return p.tertiaryContainer;
            case 1:
                return p.secondaryContainer;
            default:
                return p.surfaceContainerHighest;
        }
    }

    private int priorityOnContainer(int value) {
        switch (value) {
            case 3:
                return p.onErrorContainer;
            case 2:
                return p.onTertiaryContainer;
            case 1:
                return p.onSecondaryContainer;
            default:
                return p.onSurface;
        }
    }

    // --------------------------------------------------------------- due date

    private void refreshDueUi() {
        buildDateChips();
        buildTimeChips();
        buildRepeatChips();
        buildWeekdayChips();
        updateDueSummary();
    }

    private void buildDateChips() {
        dateChips.removeAllViews();
        long now = System.currentTimeMillis();
        boolean today = dueAt > 0 && Dates.dayDiff(dueAt, now) == 0;
        boolean tomorrow = dueAt > 0 && Dates.dayDiff(dueAt, now) == 1;

        addChip(dateChips, "Hoje", today, () -> setDueDay(dayStart(0)));
        addChip(dateChips, "Amanhã", tomorrow, () -> setDueDay(dayStart(1)));
        addChip(dateChips, "Outra data", false, this::pickDate);
        if (dueAt > 0) {
            addChip(dateChips, "Limpar", false, () -> {
                dueAt = 0;
                hasTime = false;
                refreshDueUi();
            });
        }
    }

    private void buildTimeChips() {
        timeChips.removeAllViews();
        if (dueAt <= 0) {
            addChip(timeChips, "Definir horário", false, () -> {
                dueAt = dayStart(0);
                refreshDueUi();
                pickTime();
            });
            return;
        }
        addChip(timeChips, hasTime ? Dates.time(dueAt) : "Definir horário",
                hasTime, this::pickTime);
        if (hasTime) {
            addChip(timeChips, "Limpar horário", false, () -> {
                hasTime = false;
                dueAt = Dates.startOfDay(dueAt);
                refreshDueUi();
            });
        }
    }

    private void updateDueSummary() {
        String text;
        if (dueAt <= 0) {
            text = "Sem prazo definido";
        } else {
            text = "Prazo: " + Dates.dueSummary(dueAt, hasTime);
        }
        if (repeat > 0) {
            text += "\n" + Dates.repeatSummary(repeat, weekdays);
            if (hasTime) text += " às " + Dates.time(dueAt);
        }
        dueSummary.setText(text);
    }

    private void buildRepeatChips() {
        repeatChips.removeAllViews();
        String[] labels = {"Não repetir", "Diária", "Dias úteis", "Semanal", "Mensal",
                "Por dia"};
        for (int i = 0; i < labels.length; i++) {
            final int value = i;
            addChip(repeatChips, labels[i], repeat == value, () -> {
                repeat = value;
                if (repeat > 0 && dueAt <= 0) {
                    dueAt = dayStart(0);
                    hasTime = false;
                }
                if (repeat == 5) weekdays = 0;
                refreshDueUi();
            });
        }
    }

    /** Chips de dias da semana, visíveis apenas no modo "Dias da semana". */
    private void buildWeekdayChips() {
        weekdayChips.removeAllViews();
        if (repeat != 5) {
            weekdayChips.setVisibility(View.GONE);
            return;
        }
        weekdayChips.setVisibility(View.VISIBLE);

        String[] labels = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        int[] bits = {Dates.MONDAY, Dates.TUESDAY, Dates.WEDNESDAY, Dates.THURSDAY,
                Dates.FRIDAY, Dates.SATURDAY, Dates.SUNDAY};

        LinearLayout row = null;
        for (int i = 0; i < labels.length; i++) {
            if (i % 4 == 0) {
                row = Ui.row(this);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i > 0) rowLp.topMargin = Ui.dp(this, 8);
                weekdayChips.addView(row, rowLp);
            }
            final int bit = bits[i];
            addChip(row, labels[i], (weekdays & bit) != 0, () -> {
                int next = weekdays ^ bit;
                if (next == 0) {
                    Ui.snackbar(this, p, "Escolha pelo menos um dia", null, null, 18);
                    return;
                }
                weekdays = next;
                snapDueToWeekdays();
                refreshDueUi();
            });
        }
    }

    /** Move o prazo para o próximo dia marcado, mantendo o horário. */
    private void snapDueToWeekdays() {
        if (repeat != 5 || weekdays == 0 || dueAt <= 0) return;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dueAt);
        if ((weekdays & Dates.weekdayBit(c.get(Calendar.DAY_OF_WEEK))) != 0) return;

        Calendar base = Calendar.getInstance();
        base.setTimeInMillis(dueAt);
        for (int i = 0; i < 7; i++) {
            base.add(Calendar.DAY_OF_YEAR, 1);
            if ((weekdays & Dates.weekdayBit(base.get(Calendar.DAY_OF_WEEK))) != 0) {
                base.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                base.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                base.set(Calendar.SECOND, 0);
                base.set(Calendar.MILLISECOND, 0);
                dueAt = base.getTimeInMillis();
                return;
            }
        }
    }

    private long dayStart(int offset) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Dates.startOfDay(System.currentTimeMillis()));
        c.add(Calendar.DAY_OF_YEAR, offset);
        return c.getTimeInMillis();
    }

    private void setDueDay(long startOfDay) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay);
        if (hasTime && dueAt > 0) {
            Calendar old = Calendar.getInstance();
            old.setTimeInMillis(dueAt);
            c.set(Calendar.HOUR_OF_DAY, old.get(Calendar.HOUR_OF_DAY));
            c.set(Calendar.MINUTE, old.get(Calendar.MINUTE));
            c.set(Calendar.SECOND, 0);
        } else {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
        }
        dueAt = c.getTimeInMillis();
        refreshDueUi();
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        if (dueAt > 0) c.setTimeInMillis(dueAt);
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar n = Calendar.getInstance();
            n.set(Calendar.YEAR, year);
            n.set(Calendar.MONTH, month);
            n.set(Calendar.DAY_OF_MONTH, day);
            if (hasTime && dueAt > 0) {
                Calendar old = Calendar.getInstance();
                old.setTimeInMillis(dueAt);
                n.set(Calendar.HOUR_OF_DAY, old.get(Calendar.HOUR_OF_DAY));
                n.set(Calendar.MINUTE, old.get(Calendar.MINUTE));
            } else {
                n.set(Calendar.HOUR_OF_DAY, 0);
                n.set(Calendar.MINUTE, 0);
            }
            n.set(Calendar.SECOND, 0);
            n.set(Calendar.MILLISECOND, 0);
            dueAt = n.getTimeInMillis();
            refreshDueUi();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        Ui.tintPlatformDialog(dialog, p);
        dialog.show();
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        if (dueAt > 0) c.setTimeInMillis(dueAt);
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar n = Calendar.getInstance();
            if (dueAt > 0) n.setTimeInMillis(dueAt);
            n.set(Calendar.HOUR_OF_DAY, hour);
            n.set(Calendar.MINUTE, minute);
            n.set(Calendar.SECOND, 0);
            n.set(Calendar.MILLISECOND, 0);
            dueAt = n.getTimeInMillis();
            hasTime = true;
            refreshDueUi();
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        Ui.tintPlatformDialog(dialog, p);
        dialog.show();
    }

    // --------------------------------------------------------------- save

    private boolean isDirty() {
        return !etTitle.getText().toString().equals(initialTitle)
                || !etNotes.getText().toString().equals(initialNotes)
                || !category.equals(initialCategory)
                || priority != initialPriority
                || dueAt != initialDue
                || hasTime != initialHasTime
                || repeat != initialRepeat
                || weekdays != initialWeekdays;
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Ui.shake(etTitle);
            etTitle.requestFocus();
            Ui.snackbar(this, p, "Dê um título para a tarefa", null, null, 18);
            return;
        }
        if (task == null) task = Task.create(title);
        if (repeat == 5 && weekdays == 0) {
            Ui.snackbar(this, p, "Escolha pelo menos um dia da semana", null, null, 18);
            return;
        }
        task.title = title;
        task.notes = etNotes.getText().toString().trim();
        task.category = category;
        task.priority = priority;
        task.dueAt = dueAt;
        task.hasTime = hasTime;
        task.repeat = repeat;
        task.weekdays = repeat == 5 ? weekdays : 0;
        Store.upsert(this, task);
        ReminderScheduler.schedule(this, task);
        saved = true;
        Ui.hideKeyboard(this);
        setResult(RESULT_OK);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
    }

    private void handleClose() {
        Ui.hideKeyboard(this);
        if (isDirty()) {
            Ui.confirm(this, p, "Descartar alterações?",
                    "Suas mudanças não serão salvas.", "Descartar", true, () -> {
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
                    });
            return;
        }
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        handleClose();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("dueAt", dueAt);
        outState.putBoolean("hasTime", hasTime);
        outState.putInt("priority", priority);
        outState.putString("category", category);
        outState.putInt("repeat", repeat);
        outState.putInt("weekdays", weekdays);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) Ui.hideKeyboard(this);
    }
}
