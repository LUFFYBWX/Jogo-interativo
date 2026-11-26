package com.livecraft

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import kotlin.math.sin

class GameActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState);requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
  window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  @Suppress("DEPRECATION")
  window.decorView.systemUiVisibility=(View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
  setContentView(BattleView(this))
 }
 override fun onBackPressed(){GameState.stopGame();super.onBackPressed()}
 override fun onDestroy(){GameState.onChanged=null;super.onDestroy()}
}

private class BattleView(context:android.content.Context):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private var tick=0
 private val d=resources.displayMetrics.density
 private fun dp(v:Float)=v*d
 private fun argb(v:Long)=v.toInt()
 init{GameState.onChanged={postInvalidate()}}
 override fun onDraw(c:Canvas){
  val w=width.toFloat();val h=height.toFloat();if(w<=0||h<=0)return
  tick++;val s=GameState.snapshot()
  p.shader=LinearGradient(0f,0f,w,h,argb(0xff080d26),argb(0xff21142d),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  glow(c,w*.25f,h*.42f,w*.68f,argb(0xffff552d));glow(c,w*.75f,h*.42f,w*.68f,argb(0xff258aff))
  drawHeader(c,w)
  val top=dp(105f);val bottom=h-dp(70f)
  drawArena(c,dp(10f),top,w/2-dp(5f),bottom,true)
  drawArena(c,w/2+dp(5f),top,w-dp(10f),bottom,false)
  drawFighter(c,w*.265f,h*.385f,true)
  drawFighter(c,w*.735f,h*.385f,false)
  drawWall(c,w/2+s.wall.coerceIn(-1f,1f)*w*.32f,h*.57f,h)
  s.impact?.let{
   p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(30f)
   p.setShadowLayer(dp(12f),0f,0f,Color.WHITE);p.color=Color.WHITE
   c.drawText("+${it.amount}",if(it.fighter==Fighter.LUFFY)w*.27f else w*.73f,dp(145f),p);p.clearShadowLayer()
  }
  s.winner?.let{p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=dp(38f);p.color=Color.WHITE;p.setShadowLayer(dp(18f),0f,0f,Color.YELLOW);c.drawText(if(it==Fighter.LUFFY)"LUFFY WIN!" else "NARUTO WIN!",w/2,h*.91f,p);p.clearShadowLayer()}
  drawParticles(c,w,h)
  postInvalidateDelayed(45)
 }
 private fun drawHeader(c:Canvas,w:Float){
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(29f);p.color=Color.WHITE
  c.drawText("PUSH BATTLE",w/2,dp(55f),p);p.textSize=dp(15f);p.color=argb(0xffffd083);c.drawText("ANIME DUEL",w/2,dp(83f),p)
  p.color=argb(0x44ffffff);p.strokeWidth=dp(1f);c.drawLine(w*.18f,dp(92f),w*.82f,dp(92f),p)
 }
 private fun drawArena(c:Canvas,l:Float,t:Float,r:Float,b:Float,left:Boolean){
  val base=if(left)argb(0xff692b27) else argb(0xff173d72)
  val edge=if(left)argb(0xffff7654) else argb(0xff54baff)
  p.shader=LinearGradient(l,t,r,b,if(left)argb(0xff8c382b) else argb(0xff20549a),base,Shader.TileMode.CLAMP)
  c.drawRoundRect(l,t,r,b,dp(22f),dp(22f),p);p.shader=null
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(2f);p.argb(edge);c.drawRoundRect(l,t,r,b,dp(22f),dp(22f),p);p.style=Paint.Style.FILL
  // Faint diagonal energy streaks give each side its own atmosphere.
  for(i in 0..7){val yy=t+dp(45f+i*105f);p.color=if(left)0x18ffb18a else 0x183ecaff;p.strokeWidth=dp(2f);c.drawLine(l+dp(12f),yy,r-dp(12f),yy-dp(30f),p)}
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(19f);p.color=Color.WHITE
  c.drawText(if(left)"LUFFY" else "NARUTO",(l+r)/2,bottomLabel(b),p)
 }
 private fun bottomLabel(b:Float)=b-dp(24f)
 private fun glow(c:Canvas,x:Float,y:Float,r:Float,col:Int){
  p.shader=RadialGradient(x,y,r,col,0x00000000,Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null
 }
 private fun drawWall(c:Canvas,x:Float,y:Float,h:Float){
  val ww=dp(39f);val hh=h*.27f
  glow(c,x,y,dp(135f),argb(0xffff203c))
  p.setShadowLayer(dp(18f),0f,0f,argb(0xffff1736));p.shader=LinearGradient(x-ww/2,y-hh/2,x+ww/2,y+hh/2,argb(0xffa41630),argb(0xff3e071b),Shader.TileMode.CLAMP)
  c.drawRoundRect(x-ww/2,y-hh/2,x+ww/2,y+hh/2,dp(12f),dp(12f),p);p.shader=null;p.clearShadowLayer()
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(4f);p.color=argb(0xffff3b55);c.drawRoundRect(x-ww/2,y-hh/2,x+ww/2,y+hh/2,dp(12f),dp(12f),p);p.style=Paint.Style.FILL
  p.color=argb(0xffffb5bd);p.strokeWidth=dp(2.4f);p.strokeJoin=Paint.Join.ROUND
  val crack=Path().apply{moveTo(x+dp(2f),y-hh*.37f);lineTo(x-dp(5f),y-dp(22f));lineTo(x+dp(6f),y-dp(5f));lineTo(x-dp(4f),y+dp(10f));lineTo(x+dp(3f),y+hh*.36f)}
  c.drawPath(crack,p)
  p.color=0x99ff7182.toInt();c.drawLine(x-ww*.2f,y-hh*.2f,x+ww*.2f,y-hh*.29f,p)
 }
 private fun drawFighter(c:Canvas,x:Float,y:Float,left:Boolean){
  c.save();c.translate(x,y);if(!left)c.scale(-1f,1f)
  val skin=0xffffc18c.toInt();val ink=0xff171321.toInt()
  fun path(color:Int,vararg xy:Float){p.color=color;val q=Path();q.moveTo(dp(xy[0]),dp(xy[1]));var i=2;while(i<xy.size){q.lineTo(dp(xy[i]),dp(xy[i+1]));i+=2};q.close();c.drawPath(q,p)}
  fun oval(color:Int,l:Float,t:Float,r:Float,b:Float){p.color=color;c.drawOval(dp(l),dp(t),dp(r),dp(b),p)}
  fun line(color:Int,width:Float,vararg xy:Float){p.color=color;p.strokeWidth=dp(width);p.strokeCap=Paint.Cap.ROUND;p.strokeJoin=Paint.Join.ROUND;val q=Path();q.moveTo(dp(xy[0]),dp(xy[1]));var i=2;while(i<xy.size){q.lineTo(dp(xy[i]),dp(xy[i+1]));i+=2};c.drawPath(q,p)}
  // Legs, sandals and dynamic stance
  line(ink,13f,-13f,48f,-28f,79f);line(ink,13f,15f,48f,30f,79f)
  line(0xffd99a62.toInt(),5f,-13f,48f,-28f,79f);line(0xffd99a62.toInt(),5f,15f,48f,30f,79f)
  line(ink,9f,-38f,81f,-8f,83f);line(ink,9f,18f,81f,47f,83f)
  // torso and vest / jacket
  path(if(left)0xffd92c35.toInt() else 0xfff27624.toInt(),-34f,-34f,-28f,-5f,-19f,28f,21f,28f,33f,-3f,30f,-34f,0f,-24f)
  line(0xffffb34a.toInt(),3f,-20f,-22f,-8f,17f,12f,27f)
  // neck, face and ears
  oval(skin,-20f,-89f,22f,-39f);oval(skin,-29f,-75f,-18f,-59f);oval(skin,18f,-75f,29f,-59f)
  oval(0xffffd1a3.toInt(),-24f,-112f,25f,-58f)
  // hair silhouette
  if(left){
   path(0xff17131b,-28f,-91f,-43f,-110f,-27f,-108f,-32f,-129f,-11f,-114f,-5f,-139f,7f,-116f,23f,-133f,24f,-111f,39f,-120f,31f,-91f,20f,-82f)
   // straw hat
   oval(0xff8a4b20,-39f,-137f,39f,-113f);oval(0xffffbd31,-45f,-143f,44f,-123f);line(0xffd94d35,5f,-38f,-127f,38f,-127f)
  }else{
   path(0xffffcf28,-29f,-91f,-47f,-108f,-27f,-106f,-35f,-130f,-12f,-115f,-4f,-141f,9f,-116f,24f,-136f,25f,-111f,43f,-124f,34f,-91f,20f,-82f)
   line(ink,8f,-29f,-99f,29f,-99f);line(0xffb9b9bd,3f,-31f,-99f,31f,-99f)
   line(ink,5f,23f,-107f,39f,-119f,52f,-124f);line(ink,5f,22f,-101f,43f,-108f,56f,-110f)
  }
  // Eyes, brows, nose, mouth and cheek marks
  oval(Color.WHITE,-15f,-91f,-3f,-78f);oval(Color.WHITE,5f,-91f,17f,-78f)
  oval(ink,-9f,-89f,-3f,-80f);oval(ink,8f,-89f,14f,-80f)
  line(ink,3f,-98f,13f,-101f);line(ink,1f,-72f,13f,-69f)
  if(left)line(0xffa42c2c,2f,-17f,-66f,-11f,-63f) else {line(0xffd94c42,2f,-23f,-73f,-18f,-67f);line(0xffd94c42,2f,18f,-73f,23f,-67f);line(0xffd94c42,2f,-23f,-65f,-18f,-60f);line(0xffd94c42,2f,18f,-65f,23f,-60f)}
  // Forward pushing arm, hand and fingers
  line(skin,14f,24f,-24f,51f,-14f,72f,-4f)
  oval(skin,63f,-15f,81f,2f);line(skin,6f,70f,-4f,82f,-5f);line(skin,5f,70f,4f,83f,3f)
  c.restore()
  p.textAlign=Paint.Align.CENTER;p.textSize=dp(18f);p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.color=Color.WHITE;c.drawText(if(left)"LUFFY" else "NARUTO",x,y+dp(124f),p)
 }
}
}