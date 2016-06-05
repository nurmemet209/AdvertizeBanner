package com.example.nurmemet.advertizebanner;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    List<String> list=new ArrayList<>();
    @Test
    public void addition_isCorrect() throws Exception {
        list.add("1");
        list.add("2");
        list.add("3");


       for (String str:list){
           System.out.println(str);
       }
        System.out.println("排序之后");
        String str=list.remove(0);
        list.add(str);

        for (String test:list){
            System.out.println(test);
        }

    }
}