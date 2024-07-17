package com.jd.workflow.metrics.counter;

public interface ICounter {

     public void inc(long size,String... labelValues);

}
