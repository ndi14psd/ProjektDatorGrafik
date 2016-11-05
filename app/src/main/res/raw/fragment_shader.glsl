precision highp float;
varying float z;
varying float zMax;

void main() {
    float colorRatio = z / zMax;
    vec4 finalColor;

    finalColor = vec4(0.0, 0.0, 0.3, 1.0);

    if(colorRatio >=  1.0/3.0 && colorRatio <= 2.0/3.0) {
        finalColor = vec4(0.0, 0.3, 0.0, 1.0);
    }
    else if(colorRatio > 2.0/3.0){
        finalColor = vec4(0.2, 0.0, 0.0, 1.0);
    }
    /*finalColor = vec4(colorRatio, colorRatio, colorRatio, 1.0);*/


    gl_FragColor = finalColor;
}
