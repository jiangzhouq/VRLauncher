//地球着色器
uniform mat4 uMVPMatrix; //总变换矩阵
attribute vec3 aPosition;  //顶点位置
attribute vec2 aTexCoor;    //顶点纹理坐标
varying vec2 vTextureCoord;  //用于传递给片元着色器的变量
varying vec4 vAmbient;

void main()     
{                            		
   gl_Position = uMVPMatrix * vec4(aPosition,1); //根据总变换矩阵计算此次绘制此顶点位置  
   vAmbient=vec4(1.0,1.0,1.0,1.0);
   //将顶点的纹理坐标传给片元着色器
   vTextureCoord=aTexCoor;
}                 