package model;

import java.time.Instant;

public class MessageBase extends BaseType {
    private String sender;
    private int sequence;

    private Instant time1;
    private Instant time2;
    private Instant time3;

    public Instant getTime1() {
        return time1;
    }

    public void setTime1(Instant time1) {
        this.time1 = time1;
    }

    public Instant getTime2() {
        return time2;
    }

    public void setTime2(Instant time2) {
        this.time2 = time2;
    }

    public Instant getTime3() {
        return time3;
    }

    public void setTime3(Instant time3) {
        this.time3 = time3;
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "sender='" + sender + '\'' +
                ", sequence=" + sequence +
                ", time1=" + time1 +
                ", time2=" + time2 +
                ", time3=" + time3 +
                '}';
    }
}
