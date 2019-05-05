package com.lee.thanos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewPropertyAnimator;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.recyclerview.widget.RecyclerView;

class DisappearItemAnimation extends BaseItemAnimator {
    private Bitmap loadBitmapFromView(View v) {


        if (v == null) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;
    }


    @Override
    public void animateChangeImpl(final BaseItemAnimator.ChangeInfo changeInfo) {
        final RecyclerView.ViewHolder holder = changeInfo.oldHolder;
        final View view = holder == null ? null : holder.itemView;
        final RecyclerView.ViewHolder newHolder = changeInfo.newHolder;
        final View newView = newHolder != null ? newHolder.itemView : null;
        if (view != null) {
            final ViewPropertyAnimator oldViewAnim = view.animate().setDuration(
                    getChangeDuration());
            mChangeAnimations.add(changeInfo.oldHolder);
            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX);
            oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY);
            oldViewAnim.alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    dispatchChangeStarting(changeInfo.oldHolder, true);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    oldViewAnim.setListener(null);
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setTranslationY(0);
                    dispatchChangeFinished(changeInfo.oldHolder, true);
                    mChangeAnimations.remove(changeInfo.oldHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
        }

        if (newView != null) {
            newView.setAlpha(1);
            newView.setTranslationX(0);
            newView.setTranslationY(0);
        }

        if (changeInfo.oldHolder instanceof RecyclerAdapter.ItemViewHolder
                && newHolder instanceof RecyclerAdapter.ItemStubViewHolder) {

            Bitmap bitmap = loadBitmapFromView(changeInfo.oldHolder.itemView);
            RecyclerAdapter.ItemStubViewHolder stubViewHolder = (RecyclerAdapter.ItemStubViewHolder) newHolder;
            stubViewHolder.image1.setImageBitmap(bitmap);
            stubViewHolder.image2.setImageBitmap(bitmap);
            ViewCompat.animate(stubViewHolder.image1)
                    .alpha(0)
                    .translationXBy(300)
                    .setDuration(2000)
                    .start();
            mChangeAnimations.add(changeInfo.newHolder);
            ViewCompat.animate(stubViewHolder.image2)
                    .alpha(0)
                    .translationXBy(-300)
                    .setDuration(2000)
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                            dispatchChangeStarting(changeInfo.newHolder, false);
                        }

                        @Override
                        public void onAnimationEnd(View view) {

                            dispatchChangeFinished(changeInfo.newHolder, false);
                            mChangeAnimations.remove(changeInfo.newHolder);
                            dispatchFinishedWhenDone();
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    })
                    .start();
        }

    }


}
