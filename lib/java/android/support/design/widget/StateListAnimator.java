/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.StateSet;
import java.util.ArrayList;

final class StateListAnimator {

  private final ArrayList<Tuple> tuples = new ArrayList<>();

  private Tuple lastMatch = null;
  ValueAnimator runningAnimator = null;

  private final ValueAnimator.AnimatorListener animationListener =
      new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
          if (runningAnimator == animator) {
            runningAnimator = null;
          }
        }
      };

  /**
   * Associates the given Animation with the provided drawable state specs so that it will be run
   * when the View's drawable state matches the specs.
   *
   * @param specs The drawable state specs to match against
   * @param animator The animator to run when the specs match
   */
  public void addState(int[] specs, ValueAnimator animator) {
    Tuple tuple = new Tuple(specs, animator);
    animator.addListener(animationListener);
    tuples.add(tuple);
  }

  /** Called by View */
  void setState(int[] state) {
    Tuple match = null;
    final int count = tuples.size();
    for (int i = 0; i < count; i++) {
      final Tuple tuple = tuples.get(i);
      if (StateSet.stateSetMatches(tuple.specs, state)) {
        match = tuple;
        break;
      }
    }
    if (match == lastMatch) {
      return;
    }
    if (lastMatch != null) {
      cancel();
    }

    lastMatch = match;

    if (match != null) {
      start(match);
    }
  }

  private void start(Tuple match) {
    runningAnimator = match.animator;
    runningAnimator.start();
  }

  private void cancel() {
    if (runningAnimator != null) {
      runningAnimator.cancel();
      runningAnimator = null;
    }
  }

  /**
   * If there is an animation running for a recent state change, ends it.
   *
   * <p>This causes the animation to assign the end value(s) to the View.
   */
  public void jumpToCurrentState() {
    if (runningAnimator != null) {
      runningAnimator.end();
      runningAnimator = null;
    }
  }

  static class Tuple {
    final int[] specs;
    final ValueAnimator animator;

    Tuple(int[] specs, ValueAnimator animator) {
      this.specs = specs;
      this.animator = animator;
    }
  }
}
