/*-
 *  
 * Clockwork
 *  
 * Copyright (C) 2019 - 2020 adx
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
 *  
 */

package com.creditease.adx.clockwork.common.entity;

import java.util.Objects;

public class NodeResourceUsageInfo {
    private long remainingMemoryInMB;
    private long totalMemoryInMB;
    private double cpuLoad;
    private int cpuProcessNumber;

    public NodeResourceUsageInfo() {

    }

    public NodeResourceUsageInfo(long remainingMemoryInMB, long totalMemoryInMB, double cpuLoad, int cpuProcessNumber) {
        this.remainingMemoryInMB = remainingMemoryInMB;
        this.totalMemoryInMB = totalMemoryInMB;
        this.cpuLoad = cpuLoad;
        this.cpuProcessNumber = cpuProcessNumber;
    }

    public long getRemainingMemoryInMB() {
        return remainingMemoryInMB;
    }

    public void setRemainingMemoryInMB(long remainingMemoryInMB) {
        this.remainingMemoryInMB = remainingMemoryInMB;
    }

    public long getTotalMemoryInMB() {
        return totalMemoryInMB;
    }

    public void setTotalMemoryInMB(long totalMemoryInMB) {
        this.totalMemoryInMB = totalMemoryInMB;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public int getCpuProcessNumber() {
        return cpuProcessNumber;
    }

    public void setCpuProcessNumber(int cpuProcessNumber) {
        this.cpuProcessNumber = cpuProcessNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeResourceUsageInfo)) return false;
        NodeResourceUsageInfo that = (NodeResourceUsageInfo) o;
        return getRemainingMemoryInMB() == that.getRemainingMemoryInMB() &&
                getTotalMemoryInMB() == that.getTotalMemoryInMB() &&
                Double.compare(that.getCpuLoad(), getCpuLoad()) == 0 &&
                getCpuProcessNumber() == that.getCpuProcessNumber();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getRemainingMemoryInMB(), getTotalMemoryInMB(), getCpuLoad(), getCpuProcessNumber());
    }

    @Override
    public String toString() {
        return "NodeResourceUsageInfo{" +
                "remainingMemoryInMB=" + remainingMemoryInMB +
                ", totalMemoryInMB=" + totalMemoryInMB +
                ", cpuLoad=" + cpuLoad +
                ", cpuProcessNumber=" + cpuProcessNumber +
                '}';
    }
}
