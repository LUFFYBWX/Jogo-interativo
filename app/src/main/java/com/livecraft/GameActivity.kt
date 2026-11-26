package com.livecraft

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager

class GameActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);@Suppress("DEPRECATION") window.decorView.systemUiVisibility=(View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);setContentView(BattleView(this))}
 override fun onBackPressed(){GameState.stopGame();super.onBackPressed()}
 override fun onDestroy(){GameState.onChanged=null;super.onDestroy()}
}
private class BattleView(context:android.content.Context):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private var tick=0;private val d=resources.displayMetrics.density;private fun dp(n:Float)=n*d
 init{GameState.onChanged={postInvalidate()}}
 override fun onDraw(c:Canvas){
  val w=width.toFloat();val h=height.toFloat();if(w<=0||h<=0)return;tick++;val s=GameState.snapshot()
  p.shader=LinearGradient(0f,0f,w,h,0xff080d26.toInt(),0xff281128.toInt(),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  glow(c,w*.25f,h*.35f,w*.7f,0xffff6327.toInt());glow(c,w*.75f,h*.35f,w*.7f,0xff258aff.toInt());dust(c,w,h)
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(29f);p.color=Color.WHITE;c.drawText("PUSH BATTLE",w/2,dp(55f),p);p.textSize=dp(15f);p.color=0xffffd083.toInt();c.drawText("ANIME DUEL",w/2,dp(83f),p)
  panel(c,dp(10f),dp(105f),w/2-dp(6f),h-dp(96f),0xff712c24.toInt());panel(c,w/2+dp(6f),dp(105f),w-dp(10f),h-dp(96f),0xff183e77.toInt())
  drawFighter(c,w*.26f,h*.36f,true);drawFighter(c,w*.74f,h*.36f,false)
  val wallX=w/2+s.wall.coerceIn(-1f,1f)*w*.32f;val wy=h*.56f;val wh=h*.27f;val ww=dp(35f)
  glow(c,wallX,wy,w*.26f,0xffff192f.toInt());p.color=0xff570d1b.toInt();c.drawRoundRect(wallX-ww/2,wy-wh/2,wallX+ww/2,wy+wh/2,dp(9f),dp(9f),p);p.color=0xffff394d.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=dp(4f);c.drawRoundRect(wallX-ww/2,wy-wh/2,wallX+ww/2,wy+wh/2,dp(9f),dp(9f),p);p.style=Paint.Style.FILL
  p.color=0xffffb1b4.toInt();p.strokeWidth=dp(2f);val crack=Path().apply{moveTo(wallX,wy-wh*.3f);lineTo(wallX-dp(7f),wy-dp(10f));lineTo(wallX+dp(4f),wy+dp(6f));lineTo(wallX-dp(5f),wy+wh*.3f)};c.drawPath(crack,p)
  s.impact?.let{p.textAlign=Paint.Align.CENTER;p.textSize=dp(32f);p.typeface=Typeface.DEFAULT_BOLD;p.color=Color.WHITE;c.drawText("+${it.amount}",if(it.fighter==Fighter.LUFFY)w*.27f else w*.73f,dp(145f),p)}
  s.winner?.let{p.color=Color.WHITE;p.textSize=dp(44f);p.textAlign=Paint.Align.CENTER;c.drawText(if(it==Fighter.LUFFY)"LUFFY WIN!" else "NARUTO WIN!",w/2,h*.91f,p)}
  postInvalidateDelayed(90)
 }
 private fun panel(c:Canvas,x:Float,y:Float,r:Float,b:Float,color:Int){p.color=color;c.drawRoundRect(x,y,r,b,dp(20f),dp(20f),p);p.color=0x55ffffff;p.style=Paint.Style.STROKE;p.strokeWidth=dp(2f);c.drawRoundRect(x,y,r,b,dp(20f),dp(20f),p);p.style=Paint.Style.FILL}
 private fun glow(c:Canvas,x:Float,y:Float,r:Float,color:Int){p.shader=RadialGradient(x,y,r,color,0x00000000,Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null}
 private fun dust(c:Canvas,w:Float,h:Float){for(i in 0..46){val x=(i*91f+tick*(i%3+1)*.4f)%w;val y=(i*137f+tick*(i%4+1)*.7f)%h;p.color=if(i%2==0)0x66ffb3a1 else 0x663dbbff;c.drawCircle(x,y,dp(if(i%6==0)3f else 1.2f),p)}}
 private fun drawFighter(c:Canvas,x:Float,y:Float,left:Boolean){
  c.save();c.translate(x,y);if(!left)c.scale(-1f,1f)
  p.color=0xffffd09a.toInt();c.drawRect(-dp(22f),-dp(83f),dp(22f),-dp(39f),p);p.color=if(left)0xffffc52e.toInt() else 0xffffdb33.toInt();c.drawRect(-dp(31f),-dp(108f),dp(31f),-dp(83f),p);c.drawRect(-dp(36f),-dp(100f),dp(36f),-dp(95f),p)
  p.color=if(left)0xffd91f35.toInt() else 0xff2450a8.toInt();c.drawRect(-dp(30f),-dp(39f),dp(30f),dp(18f),p);p.color=if(left)0xff236fc7.toInt() else 0xffff8c24.toInt();c.drawRect(-dp(25f),dp(18f),dp(25f),dp(62f),p)
  p.color=0xffffd09a.toInt();c.drawRect(dp(20f),-dp(30f),dp(43f),dp(4f),p);c.drawRect(dp(38f),-dp(22f),dp(58f),-dp(4f),p);p.color=0xff191827.toInt();c.drawRect(-dp(25f),dp(62f),-dp(7f),dp(83f),p);c.drawRect(dp(7f),dp(62f),dp(25f),dp(83f),p)
  c.restore();p.textAlign=Paint.Align.CENTER;p.textSize=dp(20f);p.typeface=Typeface.DEFAULT_BOLD;p.color=Color.WHITE;c.drawText(if(left)"LUFFY" else "NARUTO",x,y+dp(124f),p)
 }
}