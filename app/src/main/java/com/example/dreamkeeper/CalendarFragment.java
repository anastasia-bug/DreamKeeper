package com.example.dreamkeeper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private DatabaseHelper dbHelper;
    private RecyclerView dreamsRecyclerView;
    private List<Dream> dreamList;
    private DreamAdapter dreamAdapter;
    private TextView tvSelectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = view.findViewById(R.id.calendarView);
        tvSelectedDate = view.findViewById(R.id.vvv);
        dreamsRecyclerView = view.findViewById(R.id.recyclerViewDreams);
        dreamsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(requireContext());

        dreamAdapter = new DreamAdapter(dreamList, new DreamAdapter.OnDreamClickListener() {
            @Override
            public void onDreamClick(Dream dream) {
                Bundle bundle = new Bundle();
                bundle.putInt("dreamId", dream.getId());
                NavController navController = Navigation.findNavController(getView());
                navController.navigate(R.id.navigation_dream_detail, bundle);
            }
        });

        dreamsRecyclerView.setAdapter(dreamAdapter);

        // Устанавливаем текущую дату в TextView
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarView.getDate());
        tvSelectedDate.setText(currentDate);

        // Загружаеем данные для текущей даты
        loadDreamsForDate(currentDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String date = year + "-" + String.format("%02d",(month + 1)) + "-" + String.format("%02d",dayOfMonth);
                tvSelectedDate.setText(date); // Обновляем TextView с выбранной датой
                loadDreamsForDate(date);
            }
        });

        return view;
    }

    private void loadDreamsForDate(String date) {
        dreamList = dbHelper.getDreamsByDate(date);
        dreamAdapter.updateDreams(dreamList);
    }
}
