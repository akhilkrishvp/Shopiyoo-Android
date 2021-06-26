package customers.com.shopiyoo.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RoundRectCornerImageView10 extends androidx.appcompat.widget.AppCompatImageView {

    private float radius = 12f;
    private Path path;
    private RectF rect;

    public RoundRectCornerImageView10(Context context) {
        super(context);
        init();
    }

    public RoundRectCornerImageView10(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectCornerImageView10(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        path = new Path();

    }
    public void setRadius(float radius1){
        this.radius = radius1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}