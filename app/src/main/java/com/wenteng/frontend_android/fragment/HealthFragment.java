package com.wenteng.frontend_android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.adapter.BookAdapter;
import com.wenteng.frontend_android.model.Book;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HealthFragment extends Fragment {

    private RecyclerView chineseMedicineBooksRecyclerView;
    private RecyclerView westernMedicineBooksRecyclerView;
    private BookAdapter chineseMedicineBookAdapter;
    private BookAdapter westernMedicineBookAdapter;
    private List<Book> chineseMedicineBooks;
    private List<Book> westernMedicineBooks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        initViews(view);
        initData();
        setupRecyclerViews();
        return view;
    }

    private void initViews(View view) {
        chineseMedicineBooksRecyclerView = view.findViewById(R.id.chinese_medicine_books);
        westernMedicineBooksRecyclerView = view.findViewById(R.id.western_medicine_books);
    }

    private void initData() {
        // 初始化中医古籍数据
        chineseMedicineBooks = new ArrayList<>();
        chineseMedicineBooks.add(new Book(1, "黄帝内经", "佚名", "中医基础", "中国最早的医学典籍，传统医学四大经典著作之一。", "https://example.com/huangdi.jpg", new Date()));
        chineseMedicineBooks.add(new Book(2, "伤寒杂病论", "张仲景", "中医临床", "确立了辨证论治原则，是中医临床的基本原则。", "https://example.com/shanghan.jpg", new Date()));
        chineseMedicineBooks.add(new Book(3, "神农本草经", "佚名", "中药学", "中国现存最早的中药学著作。", "https://example.com/shennong.jpg", new Date()));
        chineseMedicineBooks.add(new Book(4, "本草纲目", "李时珍", "中药学", "集我国16世纪以前药学成就之大成。", "https://example.com/bencao.jpg", new Date()));
        chineseMedicineBooks.add(new Book(5, "针灸甲乙经", "皇甫谧", "针灸学", "中国现存最早的针灸学专著。", "https://example.com/zhenjiu.jpg", new Date()));

        // 初始化西医经典数据
        westernMedicineBooks = new ArrayList<>();
        westernMedicineBooks.add(new Book(6, "希波克拉底文集", "希波克拉底", "医学理论", "西方医学的奠基之作。", "https://example.com/hippocrates.jpg", new Date()));
        westernMedicineBooks.add(new Book(7, "人体的构造", "维萨里", "解剖学", "近代解剖学的founder。", "https://example.com/structure.jpg", new Date()));
        westernMedicineBooks.add(new Book(8, "伤寒论", "奥斯勒", "内科学", "现代内科学的奠基之作。", "https://example.com/typhoid.jpg", new Date()));
        westernMedicineBooks.add(new Book(9, "细胞病理学", "微尔啸", "病理学", "细胞病理学的创始人。", "https://example.com/cell.jpg", new Date()));
        westernMedicineBooks.add(new Book(10, "医学衷中参西录", "张锡纯", "中西医结合", "试图结合中西医理论的著作。", "https://example.com/combination.jpg", new Date()));
    }

    private void setupRecyclerViews() {
        // 设置中医古籍RecyclerView
        chineseMedicineBookAdapter = new BookAdapter(getContext(), chineseMedicineBooks);
        chineseMedicineBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        chineseMedicineBooksRecyclerView.setAdapter(chineseMedicineBookAdapter);

        // 设置西医经典RecyclerView
        westernMedicineBookAdapter = new BookAdapter(getContext(), westernMedicineBooks);
        westernMedicineBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        westernMedicineBooksRecyclerView.setAdapter(westernMedicineBookAdapter);
    }
}