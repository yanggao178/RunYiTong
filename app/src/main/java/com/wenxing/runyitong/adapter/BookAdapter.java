package com.wenxing.runyitong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.Book;

import java.text.SimpleDateFormat;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private Context context;
    private List<Book> bookList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
        android.util.Log.d("BookAdapter", "创建BookAdapter，书籍数量: " + bookList.size());
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_item, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        if (bookList == null || position >= bookList.size()) {
            android.util.Log.e("BookAdapter", "Invalid position or null bookList: " + position);
            return;
        }
        
        Book book = bookList.get(position);
        if (book == null) {
            android.util.Log.e("BookAdapter", "Book is null at position: " + position);
            return;
        }
        
        android.util.Log.d("BookAdapter", "绑定书籍 " + position + ": " + book.getName());
        
        holder.bookName.setText(book.getName() != null ? book.getName() : "未知书名");
        holder.bookAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "未知作者");
        holder.bookDescription.setText(book.getDescription() != null ? book.getDescription() : "暂无描述");

        // 使用Glide加载书籍封面
        Glide.with(context)
                .load(book.getCoverUrl())
                .placeholder(R.drawable.ic_book_placeholder)
                .into(holder.bookCover);
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookName;
        TextView bookAuthor;
        TextView bookDescription;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookDescription = itemView.findViewById(R.id.book_description);
        }
    }
}