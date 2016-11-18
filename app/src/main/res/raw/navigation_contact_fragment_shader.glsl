precision mediump float;
uniform sampler2D u_Texture;
varying vec2 v_TexCoordinate;
varying vec4 v_position;

void main() 
{
    float color_scale = 1.0f;
    float scale_x     = 1.0;
    float scale_ytop  = 1.0;
    float scale_ybot  = 1.0;
    vec4  color       = texture2D(u_Texture, v_TexCoordinate);

    scale_x     = (v_position[0] + 1.0)/0.3;
    scale_x    = scale_x > 1.0 ? 1.0 : scale_x < 0.0 ? 0.0 : scale_x;

    scale_ytop  = (v_position[1] + 1.0)/0.3;
    scale_ytop = scale_ytop > 1.0 ? 1.0 : scale_ytop < 0.0 ? 0.0 : scale_ytop;

    scale_ybot  = (1.0 - v_position[1])/0.3;
    scale_ybot = scale_ybot > 1.0 ? 1.0 : scale_ybot < 0.0 ? 0.0 : scale_ybot;

    gl_FragColor = vec4(color[0], color[1], color[2], scale_x*scale_ytop*scale_ybot*color[3]);
}