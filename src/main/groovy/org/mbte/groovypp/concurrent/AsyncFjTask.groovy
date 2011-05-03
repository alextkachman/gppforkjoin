package org.mbte.groovypp.concurrent

import jsr166y.ForkJoinTask
import jsr166y.Phaser

@Typed abstract class AsyncFjTask<V> extends ForkJoinTask<V> {
    protected AsyncFjTask parentTask

    private groovy.lang.Function0<V>  merger

    protected Phaser phaser

    private V rawResult

    final V getRawResult () { rawResult }

    protected final void setRawResult (V result) { rawResult = result }

    final void complete(V value) {
        super.complete(value)
        if(completedNormally) {
            merger = null
            parentTask?.phaser?.arrive()
        }
    }

    void completeExceptionally(Throwable ex) {
        super.completeExceptionally(ex)
        merger = null
        parentTask?.phaser?.arrive()
    }

    protected final boolean exec() {
        (phaser = [
            onAdvance: { phase, registeredParties ->
                def merged = merger?.call()
                if(!done)
                    complete(merged)
                return true
            }
        ]).register()
        try {
            merger = split()
            phaser.arrive()
            phaser.terminated
        }
        catch(Throwable e) {
            e.printStackTrace()
            completeExceptionally(e)
            return true
        }
    }

    protected abstract groovy.lang.Function0<V> split()

    protected final <R> AsyncFjTask<R> submit(AsyncFjTask<R> task) {
        phaser.register()
        pool.submit task[parentTask:this]
    }

    protected final <R,A> AsyncFjTaskWithArg<R,A> submit(A arg, AsyncFjTaskWithArg<R,A> task) {
        phaser.register()
        pool.submit task[parentTask:this, argument:arg]
    }

    protected final <R> R invoke(AsyncFjTask<R> task) {
        pool.invoke(task)
    }

    protected final <R,A> R invoke(A arg, AsyncFjTaskWithArg<R,A> task) {
        pool.invoke(task[argument: arg])
    }

    AsyncFjTask<V> clone() {
        ((AsyncFjTask<V>)super.clone())[parentTask: null, phaser: null, rawResult: null, merger:null]
    }
}
