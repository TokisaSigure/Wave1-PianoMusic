package com.example.wave1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class WaveView extends View{

	MediaPlayer player;
	Visualizer visualizer;
	byte[] waveform;
	byte[] waveform1000ms;
	int waveform1000ms_index;
	byte[] wavelet;
	int bpm;
	byte[] fft;
	boolean isupdate;
	boolean Scale = false;//�X�P�[�������o�ł��Ă��邩�A���Ȃ����̔���
	byte[] _clonefft; //fft�̃N���[���A���ёւ��p�z��(�K�v�Ȃ��\��������A���ؒ�)
	int max=0; //�ő�l���W�i�[�p�ϐ�
	int cmj=0,cma=0,dmj=0,dma=0,emj=0,ema=0,fmj=0,fma=0,gmj=0,gma=0,amj=0,bmj=0,bma=0;//�e�X�P�[���䗦���_�����p�ϐ�,mj�̓��W���[,ma�̓}�C�i�[
	boolean a=false,b=false,c=false,d=false,e=false,f=false,g=false;//�e���K����p�ϐ�
	boolean as=false,cs=false,ds=false,fs=false,gs=false;//(�ǉ���)���K���V���[�v�̎��p�̃t���O�A�h,��,�t�@,�\,����5���̂݃V���[�v����
	boolean majar=false,mainare=false; //���W���[�A�}�C�i�[�̃t���O�A���W���[�Ȃ疾�邢�A�}�C�i�[�Ȃ�Â��ȂƂ��ĔF��A�e�X�P�[���Ɏg�p�������������邩���ׂĂ݂�B

	public WaveView(Context context, MediaPlayer player){
		super(context);

		this.player = player;
    	visualizer = new Visualizer(player.getAudioSessionId());
    	visualizer.setEnabled(false);
    	visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
    	visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
			@Override
			public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
				// �g�`�̌`�ɂ��Ă���
				for (int i = 0; i < waveform.length; i++) {
					waveform[i] += 128;
				}
				updateWaveform(waveform);

				// �E�F�[�u���b�g��͌��ʐ���
				if(waveform1000ms_index >= waveform1000ms.length){
					int repetition = 2;
					byte[] wavelet = invoke(waveform1000ms);
					for (int i = 1; i < repetition; i++) {
						wavelet = invoke(wavelet);
					}
					int duration_max = 0;
					int iszeroindex_old = -1;
					for (int i = 0; i < wavelet.length; i++) {
						if(wavelet[i] != 0){
							if(iszeroindex_old != -1){
								int duration = i - iszeroindex_old;
								if(duration_max < duration){
									duration_max = duration;
								}
							}
							iszeroindex_old = i;
						}
					}
					if(duration_max != 0){
						double msper1sample = 1000.0 / (visualizer.getSamplingRate() / 1000);
						double ms = duration_max * Math.pow(2, repetition) * msper1sample;
						bpm = (int)(60000 / ms);
						if(bpm > 500){
							bpm /= 2;
						}else if(bpm < 50){
							bpm *= 2;
						}
					}else{
						bpm = 0;
					}
					updateWavelet(wavelet);
				}
			}

			@Override
			public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
				// fft[0]:			���������̒l�i�\�\�A���Ȃ킿0Hz�j
				// fft[1]:			�T���v�����O���g���̔����̎���
				// fft[2 * i]:		�𗬐����̎����isin�݂����ȁ`�`�j
				// fft[2 * i + 1]:	�𗬐����̋����icos�݂����ȁ`�`�j
				// �����ł͎����Ƌ������v�Z�ς݂̒l�ɂ��Ă���
				_clonefft = new byte[1024];
	    		for (int i = 1; i < fft.length / 2; i++) {
	    			double amplitude = Math.sqrt(Math.pow(fft[i * 2], 2) + Math.pow(fft[i * 2 + 1], 2));
	    			if(amplitude > Byte.MAX_VALUE){
	    				amplitude = Byte.MAX_VALUE;
	    			}
	    			fft[i * 2] = (byte)amplitude;
	    			fft[i * 2 + 1] = (byte)amplitude;
	    		}

	    		//�\�[�g���邽�߂�fft�f�[�^�̃R�s�[
	    		if(Scale==false){
	    			for(int loop=0;loop<fft.length-1;++loop)
	    			{
	    				_clonefft[loop]=fft[loop];
	    			}
	    		}

	    		//�o�u���\�[�g,�ő�l�Ƃ��̏ꏊ�𔭌����邽�߂̃\�[�g
	    		if(Scale==false){
	    			byte max_value;
    				byte tmp=0;
	    			for(int loop=2;loop<51;loop+=2)
	    			{
	    				//for(int loop2=50;loop2>loop;loop2-=2)
	    				//{
	    					if(tmp<_clonefft[loop] /*&& (_clonefft[loop2]!=0 && _clonefft[loop2-2]!=0)*/)
	    					{
	    						/*byte tmp = _clonefft[loop2];
	    						_clonefft[loop2]=_clonefft[loop2-2];
	    						_clonefft[loop2-2]=tmp;*/
	    						tmp = _clonefft[loop];
	    						max=loop;
	    						max_value = tmp;
	    					}
	    				//}
	    			}
	    			/*�~�̔���A�~�̎��g���͂��̔ԍ��Ɋi�[����Ă���͂��B*/
	    			/*max��14�u�~�v���u�~�v��127�̋����������Ă����ꍇ�A�܂��O��̎��g��������50�ȉ��̏ꍇ�A�u�~�v�Ɣ��f����*/
	    			while(max > 23){	// �{���␳�����Ă݂���i�G �ǋL(�Ԗ�):�O�{���Ȃǂɂ��Ή��ł���悤�ɂ��Ă݂܂���
	    				max = max / (max/23+1) ;
	    			}
	    			// �������݃e�X�g�ł��A�e�X�g
	    							// fft[max] > max_value * 0.5 �ōő�l50%�݂����ɂ��Ă݂���
	    			//�e���K���fif��,�l�͎��ۂ̃f�[�^�����ɎZ�o���Ă��邽�߁A���ۂ̒l�Ƃ͈قȂ�\�����傫���B
	    			//��肠�����̓_�~�[�Ƃ��Ă��̒l���g�p���Ă���,11������fft[max]�Ƃ��̎��͂̎��g���̊֌W�����特������o�������ɐ؂�ւ���B
	    			//�v�������̂Ń����F�}�b�N�X�����A���_�ōł����������̂͂ǂ̎��g���Ȃ̂������o���A���肷�邱�Ƃ͏o���Ȃ����B
	    			//11��10���܂łɊ֌W���𒲂ׂ�B
	    			/*if(max==12  && ((double)fft[max-2]/(double)fft[max])>=0.9 && ((double)fft[max+2]/(double)fft[max])>=0.33 && fft[max+4]/(double)fft[max] >= 16)//�h�̌��o
	    			{
	    				Log.d("���K","�h");c=true;
	    			}*/
	    			if(max==12 && ((double)fft[max-2]/(double)fft[max])<0.5 && ((double)fft[max+2]/(double)fft[max])<=0.3 && ((double)fft[max+4]/(double)fft[max])<0.2 && cs==false)//�h#�̌��o
	    			{
	    				Log.d("���K","�h#");cs=true;dmj++;emj++;fma++;amj++;bmj++;bma++;
	    			}
	    			if(max==12 && ((double)fft[max-2]/(double)fft[max])<0.5 && ((double)fft[max+2]/(double)fft[max])>=0.5 && ((double)fft[max+2]/(double)fft[max])<=0.75 && ((double)fft[max+4]/(double)fft[max])>=0.2 && d==false)//���̌��o
	    			{
	    				Log.d("���K","��");d=true;cmj++;cma++;dmj++;ema++;fmj++;gmj++;gma++;bma++;
	    			}
	    			if(max==12 && ((double)fft[max-2]/(double)fft[max])>0.39 && ((double)fft[max+2]/(double)fft[max])>=0.9 && ((double)fft[max+4]/(double)fft[max])>0.35 && ds==false )//��#�̌��o
	    			{
	    				Log.d("���K","��#");ds=true;cma++;emj++;fma++;gma++;bmj++;
	    			}
	    			if(max==14 && ((double)fft[max-2]/(double)fft[max])<=0.15 && e==false)//�~�̌��o
	    			{
	    				Log.d("���K","�~");e=true;cmj++;dmj++;dma++;emj++;ema++;fmj++;gmj++;amj++;bmj++;bma++;
	    			}
	    			if(max==16 && ((double)fft[max-2]/(double)fft[max])>=0.7 && ((double)fft[max+2]/(double)fft[max]) >= 0.40 && f==false)//�t�@�̌��o
	    			{
	    				Log.d("���K","�t�@");f=true;cmj++;cma++;dma++;fmj++;fma++;gma++;
	    			}
	    			if(max==16 && ((double)fft[max-2]/(double)fft[max])<=0.5 && ((double)fft[max+2]/(double)fft[max])<=0.5 && fs==false)//�t�@#�̌��o
	    			{
	    				Log.d("���K","�t�@#");fs=true;dmj++;emj++;ema++;gmj++;amj++;bmj++;bma++;
	    			}
	    			if(max==18 && ((double)fft[max-2]/(double)fft[max])>=0.50 && ((double)fft[max+2]/(double)fft[max]) <= 0.50 && g==false)//�\�̌��o
	    			{
	    				Log.d("���K","�\");g=true;cmj++;cma++;dmj++;dma++;ema++;fmj++;fma++;gmj++;gma++;bma++;
	    			}
	    			if(max==18 && ((double)fft[max+2]/(double)fft[max])<=0.30 && ((double)fft[max+2]/(double)fft[max])<0.5 && gs==false)//�\#�̌��o
	    			{
	    				Log.d("���K","�\#");gs=true;cma++;emj++;fma++;amj++;bmj++;
	    			}
	    			if(max==20 && ((double)fft[max-2]/(double)fft[max])>=0.5 && ((double)fft[max+2]/(double)fft[max])<0.5 && a==false)//���̌��o
	    			{
	    				Log.d("���K","��");a=true;cmj++;dmj++;dma++;emj++;ema++;fmj++;gmj++;gma++;amj++;bma++;
	    			}
	    			if(max==20 && ((double)fft[max-2]/(double)fft[max])<0.5 && ((double)fft[max+2]/(double)fft[max])<0.5 && as==false )//��#�̌��o
	    			{
	    				Log.d("���K","��#");as=true;cma++;dma++;emj++;ema++;fmj++;gma++;bmj++;
	    			}
	    			if(max==22 && ((double)fft[max-2]/(double)fft[max])>=0.5 && ((double)fft[max+2]/(double)fft[max])<=0.5 && b==false )//�V�̌��o
	    			{
	    				Log.d("���K","�V");b=true;cmj++;dmj++;emj++;ema++;gmj++;amj++;bmj++;bma++;
	    			}
	    			if(max==22 && ((double)fft[max-2]/(double)fft[max])<=0.3 && c==false )//�h�̌��o
	    			{
	    				Log.d("���K","�h");c=true;cmj++;cma++;dma++;ema++;fmj++;fma++;gmj++;gma++;dmj--;
	    			}
	    			// Log.d("", (c ? "C" : "-") + (d ? "D" : "-") + (e ? "E" : "-") + (f ? "F" : "-") + (g ? "G" : "-") + (a ? "A" : "-") + (b ? "B" : "-") + );

	    			//�e��X�P�[���̌��o���s���A�擾���ꂽ���K���Q�l�ɃX�P�[���̔�����s��
	    			if(Scale!=true){

	    				if(cmj==6)
	    				{
	    					Log.d("�X�P�[��","C���W���[�X�P�[��");
	    					majar = true;
	    					Scale=true;
	    				}
	    				if(cma==6)
	    				{
	    					Log.d("�X�P�[��","C�}�C�i�[�X�P�[��");
	    					mainare=true;
	    					Scale=true;
	    				}
	    				//�����܂�C�X�P�[��
	    				//��������D�X�P�[��
	    				if(dmj==6)
	    				{
	    					Log.d("�X�P�[��","D���W���[�X�P�[��");
	    					majar = true;
	    					Scale = true;
	    				}
	    				if(dma==6)
	    				{
	    					Log.d("�X�P�[��","D�}�C�i�[�X�P�[��");
	    					mainare = true;
	    					Scale = true;
	    				}
	    				//�����܂�D�X�P�[��
	    				//��������E�X�P�[��
	    				if(emj==6)
	    				{
	    					Log.d("�X�P�[��","D���W���[�X�P�[��");
	    					majar = true;
	    					Scale = true;
	    				}
	    				if(ema==6)
	    				{
	    					Log.d("�X�P�[��","D�}�C�i�[�X�P�[��");
	    					mainare = true;
	    					Scale = true;
	    				}
	    				//�����܂�E�X�P�[��
	    				//��������F�X�P�[��
	    				if(fmj==6)
	    				{
	    					Log.d("�X�P�[��","D���W���[�X�P�[��");
	    					majar = true;
	    					Scale = true;
	    				}
	    				if(fma==6)
	    				{
	    					Log.d("�X�P�[��","D�}�C�i�[�X�P�[��");
	    					mainare = true;
	    					Scale = true;
	    				}
	    				//��������G�X�P�[��
	    				if(gmj==6)
	    				{
	    					Log.d("�X�P�[��","D���W���[�X�P�[��");
	    					majar = true;
	    					Scale = true;
	    				}
	    				if(gma==6)
	    				{
	    					Log.d("�X�P�[��","D�}�C�i�[�X�P�[��");
	    					mainare = true;
	    					Scale = true;
	    				}
	    				//��������a�X�P�[��
	    				if(amj==6)
	    				{
	    					Log.d("�X�P�[��","D���W���[�X�P�[��");
	    					majar = true;
	    					Scale = true;
	    				}
	    				//��������b�X�P�[��
	    				if(bmj==6)
	    				{
	    					Log.d("�X�P�[��","D���W���[�X�P�[��");
	    					majar = true;
	    					Scale = true;
	    				}
	    				if(bma==6)
	    				{
	    					Log.d("�X�P�[��","D�}�C�i�[�X�P�[��");
	    					mainare = true;
	    					Scale = true;
	    				}
	    			}

	    			//if(max==20 && fft[max]==127 )//�h�̌��o
	    			//{
	    				/*�֋X�I�ȃX�P�[������,�u�~�v�̉���C���W���[�݂̂ł���ׁA�u�~�v���擾���ꂽ�ꍇ�uC���W���[�v�ƂȂ�
	    				 * �������A����ł͐��x�ɓ��*/
	    				//Toast.makeText(this, "���W���[�X�P�[��", 10000).show();
	    			/*	e=true;//�~�̎��g�����s�[�N�������ꍇ�A�~�̃t���O���I���ɂ���
	    				Log.d("scale","C���W���[�X�P�[��");
	    				Scale = true;
	    				Log.d("max",max+"");

	    				//�e�X�g�p�����A�ǂ̌��ɂǂ�Ȓl�������Ă��邩�̃`�F�b�N�p��
	    				Log.d("fft[max]",fft[max]+"");
	    				Log.d("_clonefft",_clonefft[max]+"");
	    				Log.d("max-2/max",_clonefft[max-2]+"");
	    				Log.d("max-2/max",((double)_clonefft[max-2]/(double)_clonefft[max])*100+"%");
	    				Log.d("max+2/max",((double)_clonefft[max+2]/(double)_clonefft[max])*100+"%");
	    				Log.d("max+4/max",((double)_clonefft[max+4]/(double)_clonefft[max])*100+"%");
	    				if(max>0)
	    					Log.d("fft[max-1]",fft[max-1]+"");
	    				Log.d("fft[max+1]",fft[max+1]+"");

					}*/
	    		}
				updateFFT(fft);
			}
		},
		Visualizer.getMaxCaptureRate(),
		true, true);	// waveform, fft
    	visualizer.setEnabled(true);

    	waveform = null;
    	waveform1000ms = null;
    	waveform1000ms_index = -1;
    	wavelet = null;
    	bpm = -1;
    	fft = null;
    	isupdate = true;
    }

    // �E�F�[�u���b�g�ϊ�
    private byte[] invoke(byte[] input){
    	// Haar Wevelet
//		byte[] output = new byte[input.length / 2];
//		byte[] outputw = new byte[input.length / 2];
//		for (int j = 0; j < input.length / 2 - 1; j++) {
//			int average = (input[j * 2] + input[j * 2 + 1]) / 2;
//			output[j] = (byte)average;
//			int difference = (input[j * 2] - input[j * 2 + 1]);
//			outputw[j] = (byte)difference;
//		}
		// Daubechie Wavelet
		byte[] output = new byte[input.length / 2];
		byte[] outputw = new byte[input.length / 2];
		double[] daubechiep = {0.707106781, 0.707106781};	// N=1
//		double[] daubechiep = {0.230377813, 0.714846570, 0.630880767, -0.027983769,
//								-0.187034811, 0.030841381, 0.032883011, -0.010597401};	// N=4
//		double[] daubechiep = {0.026670057, 0.188176800, 0.527201188, 0.688459039, 0.281172343,
//								-0.249846424, -0.195946274, 0.127369340, 0.093057364, -0.071394147,
//								-0.029457536, 0.033212674, 0.003606553, -0.010733175, 0.001395351,
//								0.001992405, -0.000685856, -0.000116466, 0.000093588, -0.000013264};	// N=10
		double[] daubechieq = {0.707106781, -0.707106781};	// N=1
//		double[] daubechieq = {0.010597401, -0.032883011, 0.030841381, -0.187034811,
//								0.027983769, -0.630880767, 0.714846570, -0.230377813};	// N=4
//		double[] daubechieq = {-0.000013264, -0.000093588, -0.000116466, 0.000685856, 0.001992405,
//								-0.001395351, -0.010733175, -0.003606553, 0.033212674, 0.029457536,
//								-0.071394147, -0.093057364, 0.127369340, 0.195946274, -0.249846424,
//								-0.281172343, 0.688459039, -0.527201188, 0.188176800, -0.026670057};	// N=10
		for (int i = 0; i < input.length / 2; i++) {
			output[i] = 0;
			outputw[i] = 0;
			for (int j = 0; j < daubechiep.length; j++) {
				int index = (j + 2 * i) % input.length;
				output[i] += daubechiep[j] * input[index];
				outputw[i] += daubechieq[j] * input[index];
			}
		}
    	return output;
    }

    public void onDraw(Canvas canvas){
		Paint paint = new Paint();
		if(!isupdate){
			canvas.drawText("update pausing", 0, 20, paint);
		}
		drawArray(canvas, "waveform", waveform, 1, (int)(getHeight() * 0.25));
		drawArray(canvas, "wavelet", wavelet, 1, (int)(getHeight() * 0.50));
		if(waveform1000ms_index != -1 && waveform1000ms != null){
			canvas.drawText(waveform1000ms_index + " / " + waveform1000ms.length, 0, (int)(getHeight() * 0.50) - 54, paint);
		}
		if(bpm != -1){
			canvas.drawText("BPM: " + bpm, 0, (int)(getHeight() * 0.50) + 54, paint);
		}
		drawArray(canvas, "FFT", fft, 2, (int)(getHeight() * 0.75));
//		for (int i = 0; i < fft.length; i++) {
	        // 1kHz���Ƃɖڐ���
//	        int samplingrate = visualizer.getSamplingRate() / 1000;
//	        int capturerate = visualizer.getCaptureSize();
//	        if(i % (samplingrate / capturerate / 2) == 0){
//	        	g.drawLine(x1, zero_y, x2, zero_y + 5, paint);
//	        }
//		}

    	// �A�����ĕ`�悷��
		invalidate();
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
        }
    }

    private void drawArray(Canvas canvas, String label, byte[] array, int division, int zero_y){
    	Paint paint = new Paint();
    	String length_label = "";
        int width = getWidth();
        if(array != null){
            for (int i = division != 2 ? 0 : 1; i < array.length / division; i++) {
            	int x1 = width * i / (array.length / division);
            	int y1 = zero_y;
            	int x2 = x1;
            	int y2 = zero_y - array[i * division];
    	        canvas.drawLine(x1, y1, x2, y2, paint);
            }
            length_label = "" + array.length / division;
        }
        canvas.drawText(label + " " + length_label, 0, zero_y - 64, paint);
        canvas.drawLine(0, zero_y, width, zero_y, paint);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
        	isupdate = !isupdate;
        	visualizer.setEnabled(isupdate);
        }

        return true;
    }

    public void updateWaveform(byte[] waveform){
    	if(isupdate){
        	this.waveform = waveform;
        	if(waveform1000ms_index >= 0 && waveform1000ms_index < waveform1000ms.length){
            	for (int i = 0; i < waveform.length && waveform1000ms_index + i < waveform1000ms.length; i++) {
        			waveform1000ms[waveform1000ms_index + i] = waveform[i];
        		}
            	waveform1000ms_index += waveform.length;
        	}else{
        		waveform1000ms = new byte[visualizer.getSamplingRate() / 1000 * 4];
        		waveform1000ms_index = 0;
        	}
    	}
    }

    public void updateWavelet(byte[] wavelet){
    	if(isupdate){
        	this.wavelet = wavelet;
    	}
    }

    public void updateFFT(byte[] fft){
    	if(isupdate){
        	this.fft = fft;
    	}
    }

}


