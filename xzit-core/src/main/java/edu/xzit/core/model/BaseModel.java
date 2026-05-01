package edu.xzit.core.model;

import cn.hutool.core.bean.BeanUtil;

public interface BaseModel<T> {

    default <V> V copyFrom(T t) {
        BeanUtil.copyProperties(t, this);
        return (V) this;
    }

    default T copyTo(T t) {
        BeanUtil.copyProperties(this, t);
        return t;
    }
}
