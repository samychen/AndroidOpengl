uniform mat3 u_MVPMatrix;
attribute vec4 a_Position;
attribute vec2 a_TexCoordinate;
varying vec2 v_TexCoordinate;

void main()
{
	v_TexCoordinate = a_TexCoordinate;
	gl_Position = vec4((vec3(a_Position.xy, 1.0)*u_MVPMatrix).xy, a_Position.z, 1.0);
}