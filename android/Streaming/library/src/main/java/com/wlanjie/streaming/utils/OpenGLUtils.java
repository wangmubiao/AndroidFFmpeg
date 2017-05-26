package com.wlanjie.streaming.utils;

import android.content.res.Resources;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by caowu15 on 2017/5/26.
 */

public class OpenGLUtils {

  public static String readShaderFromRawResource(Resources resources, int resourceId) {
    final InputStream inputStream = resources.openRawResource(resourceId);
    final InputStreamReader reader = new InputStreamReader(inputStream);
    final BufferedReader bufferedReader = new BufferedReader(reader);
    StringBuilder body = new StringBuilder();
    try {
      String nextLine;
      while ((nextLine = bufferedReader.readLine()) != null) {
        body.append(nextLine);
        body.append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return body.toString();
  }

  public static int loadProgram(final String vertexSource, final String fragmentSource) {
    int vertexShader = loadShader(vertexSource, GLES20.GL_VERTEX_SHADER);
    int fragmentShader = loadShader(fragmentSource, GLES20.GL_FRAGMENT_SHADER);
    int programId = GLES20.glCreateProgram();
    GLES20.glAttachShader(programId, vertexShader);
    GLES20.glAttachShader(programId, fragmentShader);
    GLES20.glLinkProgram(programId);
    GLES20.glDeleteShader(vertexShader);
    GLES20.glDeleteShader(fragmentShader);
    return programId;
  }

  public static int loadShader(final String source, final int type) {
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, source);
    GLES20.glCompileShader(shader);
    return shader;
  }
}
