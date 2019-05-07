
[github地址](https://github.com/Brooks0129/Thanos)

在手机chrome搜索灭霸后，会出现一个手套的图片，点击图片后会出现一个彩蛋，即一半的搜索结果会消失。消失的动画如下图所示：
![chrome](https://img-blog.csdnimg.cn/20190505234908127.gif)

可以看到这个动画 大致可以理解为：将当前view分为两份，每一份同时做透明度动画，并且同时向左想右移动。

先来看下我们实现的效果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190506000027878.gif)
首先是实现的思路：

如何让一个view同时向左右移动呢？我们可以创建两个一模一样的view。但是现在问题来了？如果将原来的view复制一份，合适吗？如果view的层级比较复杂，那么这样做的成本十分巨大。所以我的方案是将当前view的截图保存为Bitmap，然后在一个新的view里创建两个ImageView，将bitmap分别设置给ImageView。然后我们对两个ImageView做动画就可以了。


现在分别看下几个实现细节：

## 如何拿到View截图
用下面的方法可以拿到对应view的Bitmap，

```java
    
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


```

需要注意的是，这里需要用`Bitmap.createBitmap(v.getDrawingCache());`来创建一个新的bitmap,而不能直接使用`v.getDrawingCache()`，因为使用`v.getDrawingCache()`直接创建的bitmap会被回收掉。

## RecyclerView如何实现Item动画
RecyclerView可以通过`mRecyclerView.setItemAnimator(disappearAnimation);`方法设置Item的动画，Item的动画一共包括四种，分别是`add`、`remove`、`move`、`change`。这四种动画分别是指，添加Item、删除Item、Item移动和Item改变的时候。

那么现在问题来了，我们的这个动画适合remove效果吗？使用remove效果的话，需要执行的逻辑比较啊复杂，首先需要将item中的所有子view全部设置为不可见，然后再添加两个ImageView。这样操作的效率太低了。而且添加ImageView的方式根据item根view的layout不同而不同，这样对原有view的侵入型太强。

所以我们选择使用change效果。
最简单的使用方式，是继承`DefaultItemAnimator`，然后实现`animateChange`方法。但是这样会有一个问题，就是动画效果会出现闪一下的bug，这是由于`DefaultItemAnimator`的`animateChangeImpl`方法里对新view做了一个透明度变化的动画，而且这个方法是private的，我们不能覆盖。所以我们就不能直接继承于`DefaultItemAnimator`。
那么我们的解决方案是自己写一个`BaseItemAnimator`，其中的代码大部分与`DefaultItemAnimator`相同，但是`animateChangeImpl`方法设置为public，这样的话我们就可以在子类覆盖`animateChangeImpl`方法，从而随意定制我们的动画。这里仅贴出子类的代码。`BaseItemAnimator`的代码太长，可以参阅上面的github地址。
```java
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

```

在更新RecyclerView的时候，需要使用`notifyItemChanged`方法更新。

##  随机选一半Item
这里提供一种较为简单的方法：每次随机选一个，然后放到Set里，由于Set可以自动去重，随意当Set的大小为原有数据的一半时，那么Set里的数据就是随机一半数据。

```java
    private void randomDisappearAHalf() {

        if (mList != null && !mList.isEmpty()) {

            int size = mList.size();
            HashSet<Integer> set = new HashSet<>();
            while (set.size() < size / 2) {
                Random random = new Random();
                int anInt = random.nextInt(size);
                set.add(anInt);
            }

            for (Integer i : set) {
                mList.get(i).type = 1;
                mRecyclerAdapter.notifyItemChanged(i);
            }


        }
    }


```