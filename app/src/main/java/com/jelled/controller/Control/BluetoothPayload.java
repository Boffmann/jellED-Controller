package com.jelled.controller.Control;

import com.google.gson.annotations.SerializedName;

public class BluetoothPayload {
    @SerializedName("p1")
    private int patternType1;
    @SerializedName("p2")
    private int patternType2;
    @SerializedName("p3")
    private int patternType3;
    @SerializedName("p4")
    private int patternType4;
    @SerializedName("bpp")
    private int beatsPerPattern;
    @SerializedName("c1")
    private int color1;
    @SerializedName("c2")
    private int color2;
    @SerializedName("c3")
    private int color3;

    private BluetoothPayload(final Builder builder) {
        this.patternType1 = builder.patternType1;
        this.patternType2 = builder.patternType2;
        this.patternType3 = builder.patternType3;
        this.patternType4 = builder.patternType4;
        this.beatsPerPattern = builder.beatsPerPattern;
        this.color1 = builder.color1;
        this.color2 = builder.color2;
        this.color3 = builder.color3;
    }

    public static class Builder {
        private int patternType1;
        private int patternType2;
        private int patternType3;
        private int patternType4;
        private int beatsPerPattern;
        private int color1;
        private int color2;
        private int color3;

        public Builder withPatternType1(final PatternType patternType) {
            this.patternType1 = patternType.getId();
            return this;
        }

        public Builder withPatternType2(final PatternType patternType) {
            this.patternType2 = patternType.getId();
            return this;
        }

        public Builder withPatternType3(final PatternType patternType) {
            this.patternType3 = patternType.getId();
            return this;
        }

        public Builder withPatternType4(final PatternType patternType) {
            this.patternType4 = patternType.getId();
            return this;
        }

        public Builder withBeatsPerPattern(final int beatsPerPattern) {
            this.beatsPerPattern = beatsPerPattern;
            return this;
        }

        public Builder withColor1(final int color) {
            this.color1 = color;
            return this;
        }

        public Builder withColor2(final int color) {
            this.color2 = color;
            return this;
        }

        public Builder withColor3(final int color) {
            this.color3 = color;
            return this;
        }

        public BluetoothPayload build() {
            return new BluetoothPayload(this);
        }

    }

}
