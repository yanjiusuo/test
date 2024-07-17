package com.jd.workflow.console.base;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 判空工具类
 * @author wubaizhao1
 * @date 2021/9/18 16:25
 */
public class EmptyUtil {
    /**
     * 判断是否存在空
     * {@link EmptyUtil#test1_normal() 测试用例}
     * 1.字符串-String StringBuilder StringBuffer         :支持
     * "","  ","  s","s  ","正常字符串"
     * 2.List Collection-empty                          :支持
     * 3.Map                                            :支持
     * 4.Array                                          :不支持
     * @date 2021/9/29 9:47
     * @author wubaizhao1
     * @param objects 参数们
     * @return boolean
     */
    public static boolean isAnyEmpty(Object... objects){
        if (isArrayEmpty(objects)) {
            return true;
        }
        for (Object object : objects){
            if(isEmpty(object)){
                return true;
            }
        }
        return false;
    }
    /**
     * 判断是否全空
     * @date 2021/9/29 9:47
     * @author wubaizhao1
     * @param objects
     * @return
     */
    public static boolean isAllEmpty(Object... objects){
        if (isArrayEmpty(objects)) {
            return true;
        }
        for (Object object : objects){
            if(!isEmpty(object)){
                return false;
            }
        }
        return true;
    }
    /**
     * 单元素判空
     * @date 2021/9/29 9:47
     * @author wubaizhao1
     * @param object
     * @return
     */
    public static boolean isEmpty(Object object){
        if(Objects.isNull(object)){
            return true;
        }else if(object instanceof CharSequence){
            if (isBlank((CharSequence)object)) {
                return true;
            }
        }else if(object instanceof Collection){
            if (isCollectionEmpty((Collection)object)) {
                return true;
            }
        }else if(object instanceof Map){
            if (isMapEmpty((Map)object)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是否不为空
     * @date 2021/11/19 16:12
     * @author wubaizhao1
     * @param object
     * @return
     */
    public static boolean isNotEmpty(Object object){
        return !isEmpty(object);
    }
    /**
     * 字符串判空
     * @date 2021/9/29 9:45
     * @author wubaizhao1
     * @param cs
     * @return
     */
    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
    /**
     * 数字判空
     * @date 2021/9/29 9:46
     * @author wubaizhao1
     * @param num
     * @return boolean
     */
    public static boolean isNumEmptyOrZero(Integer num){
        if(isEmpty(num) || num == 0){
            return true;
        }
        return false;
    }
    /**
     * JSON字符串判空
     * @date 2021/9/29 9:46
     * @author wubaizhao1
     * @param JSONString JSON字符串
     * @return boolean
     */
    public static boolean isJSONStingEmpty(String JSONString){
        if(isBlank(JSONString) || "{}".equals(JSONString)){
            return true;
        }
        return false;
    }
    /**
     * ArrayUtils
     * @date 2021/9/29 10:47
     * @author wubaizhao1
     * @param array
     * @return
     */
    private static boolean isArrayEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }
    
    /**
     * MapUtils 判空
     * @date 2021/9/29 10:48
     * @author wubaizhao1
     * @param map
     * @return
     */
    private static boolean isMapEmpty(final Map<?,?> map) {
        return map == null || map.isEmpty();
    }
    /**
     * CollectionUtils判空
     * @date 2021/9/29 10:53
     * @author wubaizhao1
     * @param collection
     * @return
     */
    private static boolean isCollectionEmpty(Collection<?> collection) {return (collection == null || collection.isEmpty());}
    private static boolean isCollectionEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
    
    //单元测试
    public static void main(String[] args) {
//        test1_normal();
        test2_array();
    }
    
    /**
     * 普通判空的单元测试
     */
    /**
     * 测试用例集
     * 1.字符串-String StringBuilder StringBuffer        :支持
     * "","  ","  s","s  ","正常字符串"
     * 2.List Collection-empty                          :支持
     * 3.Map                                            :支持
     * 4.Array                                          :不支持
     */
    private static void test1_normal(){
        //1.字符串测试
        System.out.println(EmptyUtil.isAnyEmpty(null));//true
        System.out.println(EmptyUtil.isAnyEmpty(null, "foo"));//true
        System.out.println(EmptyUtil.isAnyEmpty("", "bar"));//true
        System.out.println(EmptyUtil.isAnyEmpty("bob", ""));//true
        System.out.println(EmptyUtil.isAnyEmpty("  bob  ", null));//true
        System.out.println(EmptyUtil.isAnyEmpty(" ", "bar"));//true
        System.out.println(EmptyUtil.isAnyEmpty("foo", "bar"));//false
        System.out.println(EmptyUtil.isAnyEmpty(new StringBuffer("nihao")));//false
        System.out.println(EmptyUtil.isAnyEmpty(new StringBuffer("")));//true
        System.out.println("---------------------------------------------------------------------");
        //2.list 测试 Collections
        List<Integer> list1= new ArrayList<Integer>();
        List<Integer> list2= new ArrayList<Integer>();
        list2.add(2);
        System.out.println(EmptyUtil.isAnyEmpty(list1));//true
        System.out.println(EmptyUtil.isAnyEmpty(list2));//false
        System.out.println(EmptyUtil.isAnyEmpty(list1,list2));//true
        System.out.println(EmptyUtil.isAnyEmpty(Collections.emptyList(),list2));//true
        System.out.println(EmptyUtil.isAnyEmpty(Collections.emptyMap(),list2));//true
        System.out.println(EmptyUtil.isAnyEmpty(Collections.emptySet(),list2));//true
        System.out.println(EmptyUtil.isAnyEmpty(Collections.emptyEnumeration(),list2));//false
        System.out.println(EmptyUtil.isAnyEmpty(Collections.emptySortedMap(),list2));//true
        System.out.println(EmptyUtil.isAnyEmpty(Collections.emptySortedSet(),list2));//true
        System.out.println(EmptyUtil.isAnyEmpty(Collections.emptyIterator(),list2));//false
        System.out.println("---------------------------------------------------------------------");
        //3.Map测试
        Map<String,String> map1=new HashMap<>();
        Map<String,String> map2=new HashMap<>();
        map2.put("testkey","testvalue");
        System.out.println(EmptyUtil.isAnyEmpty(map1));//true
        System.out.println(EmptyUtil.isAnyEmpty(map2));//false
        System.out.println(EmptyUtil.isAnyEmpty(map1,map2));//true
        System.out.println("---------------------------------------------------------------------");
        //4.Arrays
        int[] arr1=new int[10];
        int[] arr2=new int[10];
        int[] arr3=new int[10];
        arr2[0]=1111;
        arr3[5]=2222;
        System.out.println(EmptyUtil.isAnyEmpty(arr1));//false
        System.out.println(EmptyUtil.isAnyEmpty(arr2));//false
        System.out.println(EmptyUtil.isAnyEmpty(arr3));//false
    }
    private static void test2_array(){
        //4.Arrays
        int[] arr1=new int[10];
        int[] arr2=new int[10];
        int[] arr3=new int[10];
        arr2[0]=1111;
        arr3[5]=2222;
        System.out.println(EmptyUtil.isAnyEmpty(arr1));//false
        System.out.println(EmptyUtil.isAnyEmpty(arr2));//false
        System.out.println(EmptyUtil.isAnyEmpty(arr3));//false
    }
    /**
     * 对象链式判断isAnyEmpty
     * @param object
     * @param <T>
     * @return
     */
    public static <T> EmptyUtilTemp<T> make(T object){
        return new EmptyUtilTemp<>(object);
    }
    
    /**
     * 链式调用帮助类
     * @param <T>
     */
    public static class EmptyUtilTemp<T>{
        T object;
        boolean emptyFlag=false;
        
        public EmptyUtilTemp(T object) {
            this.object = object;
            if(Objects.isNull(object)){
                emptyFlag=true;
            }
        }
        public static EmptyUtilTemp getEmptyOne(){
            return new EmptyUtilTemp(null);
        }
        
        /**
         * 该类型有助于在函数式编程中使用
         * @date 2021/9/24 20:18
         * @author wubaizhao1
         * @param mapper
         * @param <R>
         * @return
         */
        public <R> EmptyUtilTemp<R> map(Function<? super T, ? extends R> mapper){
            if (!emptyFlag && mapper != null){
                return new EmptyUtilTemp(mapper.apply(object));
            }
            return getEmptyOne();
        }
        
        /**
         * 在链式调用中更换对象，但是不更换flag值
         * 换另一个对象，继承是否为空的flag
         * @param object
         * @param <I>
         * @return
         */
        public <I> EmptyUtilTemp<I> mark(I object){
            if (!emptyFlag && isEmpty(object)){
                emptyFlag=true;
            }
            EmptyUtilTemp<I> temp=new EmptyUtilTemp<I>(object);
            temp.setEmptyFlag(emptyFlag);
            return temp;
        }
        
        /**
         * 链式调用判断是否存在一个为空
         * @date 2021/9/24 20:16
         * @author wubaizhao1
         * @param supplier
         * @return
         */
        public EmptyUtilTemp<T> link(Supplier<Object> supplier){
            if (!emptyFlag && supplier != null){
                if(isEmpty(supplier.get())){
                    emptyFlag=true;
                }
            }
            return this;
        }
        /**
         * 链式调用判断是否存在一个为空
         * @date 2021/9/24 20:16
         * @author wubaizhao1
         * @param object
         * @return
         */
        public EmptyUtilTemp<T> link(Object object){
            if (!emptyFlag){
                if(isEmpty(object)){
                    emptyFlag=true;
                }
            }
            return this;
        }
        public boolean isAnyEmpty(){
            return emptyFlag;
        }
        
        
        private void setEmptyFlag(boolean emptyFlag) {
            this.emptyFlag = emptyFlag;
        }
        
        /**
         * 如果非空则执行
         * @date 2021/9/24 20:15
         * @author wubaizhao1
         * @param consumer
         * @return
         */
        public EmptyUtilTemp<T> ifPresent(Consumer<? super T> consumer) {
            if ((isNotEmpty(consumer) && !emptyFlag)) {
                consumer.accept(object);
            }
            return this;
        }
        
        /**
         * 如果为空则执行
         * @date 2021/9/24 20:15
         * @author wubaizhao1
         * @param consumer
         * @return
         */
        public EmptyUtilTemp<T> orElse(Consumer<T> consumer) {
            if (isNotEmpty(consumer) && emptyFlag) {
                consumer.accept(null);
            }
            return this;
        }
        
        /**
         * 取出值或者默认值
         * @date 2021/9/24 20:15
         * @author wubaizhao1
         * @param other
         * @return
         */
        public T orElse(T other) {
            return emptyFlag ? other : object;
        }
        
        /**
         * @date 2021/10/18 17:37
         * @author wubaizhao1
         * @param other
         * @return
         */
//        public T orElseGet(Supplier<? extends T> other) {
//            return emptyFlag ? other.get() : object ;
//        }
    }
}