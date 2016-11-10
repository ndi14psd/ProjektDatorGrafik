precision mediump float;
varying float z;
varying float zMax;

varying vec4 c;

void main() {
    float colorRatio = z / zMax;
    vec4 finalColor;

    finalColor = vec4(0.0, 0.0, colorRatio + 0.3, 1.0);

    if(colorRatio >=  1.0/3.0 && colorRatio <= 2.0/3.0) {
        finalColor = vec4(0.0, 0.67 - colorRatio, 0.0, 1.0);
    }
    else if(colorRatio > 2.0/3.0){
        finalColor = vec4(colorRatio - 0.6, 0.0, 0.0, 1.0);
    }
    /*finalColor = vec4(1.0 - colorRatio, colorRatio * colorRatio, colorRatio / 2, 1.0);    /*Black and white*/

    gl_FragColor = finalColor;
}
