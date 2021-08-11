package com.creditease.adx.clockwork.common.entity.graph;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:05 下午 2020/12/3
 * @ Description：basic link
 * @ Modified By：
 */
public class Link {

    private String source;
    private String target;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "Link{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
