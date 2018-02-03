package com.unitslink.acde;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gugia on 2018/1/28.
 */

public class AirConditioning {
    private String id;

    /**
     * 名称
     */
    private String name = "carshow";

    /**
     * 模式，1=cold，2=hot，3=vent
     */
    private Integer mode = 1;

    /**
     * 设定温度
     */
    private Integer temperatureSet = 20;

    /**
     * 实际温度
     */
    private Integer temperatureActual = 17;

    /**
     * 风扇转速
     */
    private Integer fanspeed = 3;

    /**
     * 四通阀
     */
    private Integer fourwayValve = 0;

    /**
     * 电磁阀开关
     */
    private Boolean solenoidValve = true;

    /**
     * 膨胀阀开关
     */
    private Boolean txValve = true;

    /**
     * 冷凝器
     */
    private Integer condenser = 0;

    /**
     * 蒸发器
     */
    private Integer evaporator = 0;

    /**
     * 电压报警
     */
    private Boolean voltAlarm = false;

    /**
     * 压力报警
     */
    private Boolean pressureAlarm = false;

    /**
     * 电流报警
     */
    private Boolean currentAlarm = false;

    /**
     * 温度报警
     */
    private Boolean temperatureAlarm = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getTemperatureSet() {
        return temperatureSet;
    }

    public void setTemperatureSet(Integer temperatureSet) {
        this.temperatureSet = temperatureSet;
    }

    public Integer getTemperatureActual() {
        return temperatureActual;
    }

    public void setTemperatureActual(Integer temperatureActual) {
        this.temperatureActual = temperatureActual;
    }

    public Integer getFanspeed() {
        return fanspeed;
    }

    public void setFanspeed(Integer fanspeed) {
        this.fanspeed = fanspeed;
    }

    public Integer getFourwayValve() {
        return fourwayValve;
    }

    public void setFourwayValve(Integer fourwayValve) {
        this.fourwayValve = fourwayValve;
    }

    public Boolean getSolenoidValve() {
        return solenoidValve;
    }

    public void setSolenoidValve(Boolean solenoidValve) {
        this.solenoidValve = solenoidValve;
    }

    public Boolean getTxValve() {
        return txValve;
    }

    public void setTxValve(Boolean txValve) {
        this.txValve = txValve;
    }

    public Integer getCondenser() {
        return condenser;
    }

    public void setCondenser(Integer condenser) {
        this.condenser = condenser;
    }

    public Integer getEvaporator() {
        return evaporator;
    }

    public void setEvaporator(Integer evaporator) {
        this.evaporator = evaporator;
    }

    public Boolean getVoltAlarm() {
        return voltAlarm;
    }

    public void setVoltAlarm(Boolean voltAlarm) {
        this.voltAlarm = voltAlarm;
    }

    public Boolean getPressureAlarm() {
        return pressureAlarm;
    }

    public void setPressureAlarm(Boolean pressureAlarm) {
        this.pressureAlarm = pressureAlarm;
    }

    public Boolean getCurrentAlarm() {
        return currentAlarm;
    }

    public void setCurrentAlarm(Boolean currentAlarm) {
        this.currentAlarm = currentAlarm;
    }

    public Boolean getTemperatureAlarm() {
        return temperatureAlarm;
    }

    public void setTemperatureAlarm(Boolean temperatureAlarm) {
        this.temperatureAlarm = temperatureAlarm;
    }
}
