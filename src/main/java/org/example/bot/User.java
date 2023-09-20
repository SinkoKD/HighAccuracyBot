package org.example.bot;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String name;
    private String UID;
    private boolean registered;
    private boolean deposited;
    private Date lastTimeTexted;
    private Date lastTimePressedDeposit;
    private int timesTextWasSent;
    private boolean canWriteToSupport;
    private boolean canPressDeposit;
    private boolean canPressRegister;


    public boolean isCanWriteToSupport() {
        return canWriteToSupport;
    }

    public void setCanWriteToSupport(boolean canWriteToSupport) {
        this.canWriteToSupport = canWriteToSupport;
    }

    public User(String name, String UID, boolean registered, boolean deposited, Date lastTimeTexted,
                Date lastTimePressedDeposit, int timesTextWasSent, boolean canWriteToSupport,
                boolean canPressDeposit, boolean canPressRegister) {
        this.name = name;
        this.UID = UID;
        this.registered = registered;
        this.deposited = deposited;
        this.lastTimeTexted = lastTimeTexted;
        this.lastTimePressedDeposit = lastTimePressedDeposit;
        this.timesTextWasSent = timesTextWasSent;
        this.canWriteToSupport = canWriteToSupport;
        this.canPressDeposit = canPressDeposit;
        this.canPressRegister = canPressRegister;
    }

    public Date getLastTimePressedDeposit() {
        return lastTimePressedDeposit;
    }

    public void setLastTimePressedDeposit(Date lastTimePressedDeposit) {
        this.lastTimePressedDeposit = lastTimePressedDeposit;
    }

    public boolean isCanPressDeposit() {
        return canPressDeposit;
    }

    public void setCanPressDeposit(boolean canPressDeposit) {
        this.canPressDeposit = canPressDeposit;
    }

    public boolean isCanPressRegister() {
        return canPressRegister;
    }

    public void setCanPressRegister(boolean canPressRegister) {
        this.canPressRegister = canPressRegister;
    }

    public String getUID() {
        return UID;
    }

    public int getTimesTextWasSent() {
        return timesTextWasSent;
    }

    public void setTimesTextWasSent(int timesTextWasSent) {
        this.timesTextWasSent = timesTextWasSent;
    }

    public Date getLastTimeTexted() {
        return lastTimeTexted;
    }

    public void setLastTimeTexted(Date lastTimeTexted) {
        this.lastTimeTexted = lastTimeTexted;
    }

    public boolean isDeposited() {
        return deposited;
    }

    public void setDeposited(boolean deposited) {
        this.deposited = deposited;
    }

    public User() {
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}