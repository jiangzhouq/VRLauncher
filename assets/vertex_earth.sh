//������ɫ��
uniform mat4 uMVPMatrix; //�ܱ任����
attribute vec3 aPosition;  //����λ��
attribute vec2 aTexCoor;    //������������
varying vec2 vTextureCoord;  //���ڴ��ݸ�ƬԪ��ɫ���ı���
varying vec4 vAmbient;

void main()     
{                            		
   gl_Position = uMVPMatrix * vec4(aPosition,1); //�����ܱ任�������˴λ��ƴ˶���λ��  
   vAmbient=vec4(1.0,1.0,1.0,1.0);
   //��������������괫��ƬԪ��ɫ��
   vTextureCoord=aTexCoor;
}                 