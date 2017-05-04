/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.utils;

import java.io.Serializable;

/**
 *
 * @author mfilatov
 * @param <A>
 * @param <B>
 */
public class Tuple <A, B> implements Serializable{

    public final A a;
    public final B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        if (!a.equals(tuple.a)) return false;
        return b.equals(tuple.b);
    }

    @Override
    public int hashCode() {
        int result = a.hashCode();
        result = 31 * result + b.hashCode();
        return result;
    }

}
