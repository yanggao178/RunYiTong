package com.wenteng.frontend_android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.model.Book;

import java.util.List;

public class BookshelfAdapter extends RecyclerView.Adapter<BookshelfAdapter.BookViewHolder> {
    private static final String TAG = "BookshelfAdapter";
    private List<Book> books;
    private Context context;
    private OnBookClickListener onBookClickListener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookshelfAdapter(Context context, List<Book> books) {
        this.context = context;
        this.books = books;
        Log.d(TAG, "BookshelfAdapter created with " + (books != null ? books.size() : 0) + " books");
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.onBookClickListener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bookshelf_item, parent, false);
        Log.d(TAG, "Creating new BookViewHolder");
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        Log.d(TAG, "Binding book: " + book.getName() + " at position " + position);
        
        holder.bookName.setText(book.getName());
        holder.bookAuthor.setText(book.getAuthor());
        
        // 使用Glide加载书籍封面
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(12))
                            .placeholder(R.drawable.ic_book_placeholder)
                            .error(R.drawable.ic_book_placeholder))
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.ic_book_placeholder);
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onBookClickListener != null) {
                onBookClickListener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = books != null ? books.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public void updateBooks(List<Book> newBooks) {
        Log.d(TAG, "Updating books list. New size: " + (newBooks != null ? newBooks.size() : 0));
        this.books = newBooks;
        notifyDataSetChanged();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookName;
        TextView bookAuthor;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            bookAuthor = itemView.findViewById(R.id.book_author);
        }
    }
}