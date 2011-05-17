package org.mbte.groovypp.concurrent

import jsr166y.ForkJoinPool

@Trait abstract class ForkJoinCategory {

    /**
     * Performs the given task with given argument, returning its result upon completion.
     */
    static <V,A> V invokeTask(ForkJoinPool pool, A argument, AsyncFjTaskWithArg<V,A> task) {
        pool.invoke(task[argument:argument])
    }

    /**
     * Performs the given task, returning its result upon completion
     */
    static <V> V invokeTask(ForkJoinPool pool, AsyncFjTask<V> task) {
        pool.invoke task
    }

    /**
     * Submits a asynchronous task for execution
     */
    static <V> AsyncFjTask<V> submitTask(ForkJoinPool pool, AsyncFjTask<V> task) {
        pool.submit task
    }

    /**
     * Performs the given mapping on Iterator and return List of results
     */
    static <V,E> List<V> invokeMap(ForkJoinPool pool, Iterator<E> iterator, AsyncFjTaskWithArg<V,E> task) {
        pool.invokeTask {
            List<AsyncFjTask> tasks = []
            for(e in iterator)
                tasks << ((AsyncFjTask)this).submitTask(e, task.clone())

            return {
                tasks*.join()
            }
        }
    }

    /**
     * Performs the given mapping on Iterable and return List of results
     */
    static <V,E> List<V> invokeMap(ForkJoinPool pool, Iterable<E> iterable, AsyncFjTaskWithArg<V,E> task) {
        pool.invokeMap(iterable.iterator(), task)
    }

    /**
     * Performs the given mapping on Iterator and return List of results
     */
    static <V,E> AsyncFjTask<List<V>> submitMap(ForkJoinPool pool, Iterator<E> iterator, AsyncFjTaskWithArg<V,E> task) {
        pool.submitTask {
            List<AsyncFjTask> tasks = []
            for(e in iterator)
                tasks << ((AsyncFjTask)this).submitTask(e, task.clone())

            return {
                tasks*.join()
            }
        }
    }

    /**
     * Performs the given mapping on Iterable and return List of results
     */
    static <V,E> AsyncFjTask<List<V>> submitMap(ForkJoinPool pool, Iterable<E> iterable, AsyncFjTaskWithArg<V,E> task) {
        pool.submitMap(iterable.iterator(), task)
    }
}
