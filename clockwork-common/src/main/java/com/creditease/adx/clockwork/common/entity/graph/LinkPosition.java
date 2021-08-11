package com.creditease.adx.clockwork.common.entity.graph;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:43 下午 2020/8/10
 * @ Description：附带位置的图关系
 * @ Modified By：
 */
public class LinkPosition extends Link {

    private String name;

    private LineStyle lineStyle = new LineStyle();  // 样式


    public LinkPosition(String source, String target, String name) {
        super.setSource(source);
        super.setTarget(target);
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
    }

    public LineStyle getLineStyle() {
        return this.lineStyle;
    }

    @Override
    public boolean equals(Object o) {
        if (null != o) {
            LinkPosition links = (LinkPosition) o;
            return links.getName().equals(this.getName())
                    && links.getSource().equals(this.getSource())
                    && links.getTarget().equals(this.getTarget());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.getSource() != null ? super.getSource().hashCode() : 0;
        result = 31 * result + (super.getTarget() != null ? super.getTarget().hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    static class LineStyle {
        private double curveness = 0.1;

        public void setCurveness(double curveness) {
            this.curveness = curveness;
        }

        public double getCurveness() {
            return this.curveness;
        }
    }
}
