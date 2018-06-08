/**
 * Copyright (C) 2014-2015 Thundersoft Corporation
 * All rights Reserved
 */
precision mediump float;

varying highp vec2 vTextureCoord;
uniform sampler2D texture;
uniform sampler2D resourceTexture;
uniform sampler2D resourceTexture1;
uniform float fpercent;

vec3 lookup  (vec3 textureColor) {
	
	textureColor.r = clamp(textureColor.r, 0.05, 0.95);
	textureColor.g = clamp(textureColor.g, 0.05, 0.95);
	textureColor.b = clamp(textureColor.b, 0.05, 0.95);

	float blueColor = textureColor.b * 63.0;

	vec2 quad1;
	quad1.y = floor(floor(blueColor) / 8.0);
	quad1.x = floor(blueColor) - (quad1.y * 8.0);
	quad1.x = clamp(quad1.x, 0.0, 7.0);

	vec2 quad2;
	quad2.y = floor(ceil(blueColor) / 8.0);
	quad2.x = ceil(blueColor) - (quad2.y * 8.0);
	quad2.x = clamp(quad2.x, 0.0, 7.0);

	vec2 texPos1;
	texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
	texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

	vec2 texPos2;
	texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
	texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

	vec3 newColor1 = texture2D(resourceTexture, texPos1).rgb;
	vec3 newColor2 = texture2D(resourceTexture, texPos2).rgb;
	
	vec3 newColor = mix(newColor1, newColor2, fract(blueColor));
	
	newColor = newColor * 0.71 + textureColor * (1.0 - 0.71); 
	
	return newColor; 
}

void main() {

    vec3 orig = texture2D (texture,vTextureCoord).rgb;
    vec3 rgb = lookup(orig);

    rgb = rgb * fpercent + orig * (1.0 - fpercent);
    gl_FragColor = vec4(rgb, 1.0);

}