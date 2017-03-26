package com.seg3125.noteapp;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

/**
 * Provides a binding holder, which holds a binding extending {@link ViewDataBinding}.
 *
 * @param <B> the binding to be held.
 */
class BindingHolder<B extends ViewDataBinding> extends RecyclerView.ViewHolder {

    protected final B binding;

    public BindingHolder(B binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
