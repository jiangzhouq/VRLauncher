//������ɫ��
precision mediump float;
varying vec2 vTextureCoord;//���մӶ�����ɫ�������Ĳ���
varying vec4 vAmbient;
uniform sampler2D sTextureDay;//������������
void main()                         
{  
  //����ƬԪ�������в�������ɫֵ   
  vec4 finalColorDay;   
  vec4 finalColorNight;   
  
  finalColorDay= texture2D(sTextureDay, vTextureCoord);
  finalColorDay = finalColorDay*vAmbient;
  gl_FragColor=finalColorDay;    
 
}              