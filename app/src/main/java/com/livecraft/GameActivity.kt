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
 private fun color(v:Long)=v.toInt()
 init{GameState.onChanged={postInvalidate()}}
 override fun onDraw(c:Canvas){
  val w=width.toFloat();val h=height.toFloat();if(w<=0||h<=0)return
  tick++;val s=GameState.snapshot()
  p.shader=LinearGradient(0f,0f,w,h,color(0xff080d26),color(0xff21142d),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  glow(c,w*.25f,h*.42f,w*.68f,color(0xffff552d));glow(c,w*.75f,h*.42f,w*.68f,color(0xff258aff))
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
  c.drawText("PUSH BATTLE",w/2,dp(55f),p);p.textSize=dp(15f);p.color=color(0xffffd083);c.drawText("ANIME DUEL",w/2,dp(83f),p)
  p.color=color(0x44ffffff);p.strokeWidth=dp(1f);c.drawLine(w*.18f,dp(92f),w*.82f,dp(92f),p)
 }
 private fun drawArena(c:Canvas,l:Float,t:Float,r:Float,b:Float,left:Boolean){
  val base=if(left)color(0xff692b27) else color(0xff173d72)
  val edge=if(left)color(0xffff7654) else color(0xff54baff)
  p.shader=LinearGradient(l,t,r,b,if(left)color(0xff8c382b) else color(0xff20549a),base,Shader.TileMode.CLAMP)
  c.drawRoundRect(l,t,r,b,dp(22f),dp(22f),p);p.shader=null
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(2f);p.color(edge);c.drawRoundRect(l,t,r,b,dp(22f),dp(22f),p);p.style=Paint.Style.FILL
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
  glow(c,x,y,dp(135f),color(0xffff203c))
  p.setShadowLayer(dp(18f),0f,0f,color(0xffff1736));p.shader=LinearGradient(x-ww/2,y-hh/2,x+ww/2,y+hh/2,color(0xffa41630),color(0xff3e071b),Shader.TileMode.CLAMP)
  c.drawRoundRect(x-ww/2,y-hh/2,x+ww/2,y+hh/2,dp(12f),dp(12f),p);p.shader=null;p.clearShadowLayer()
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(4f);p.color=color(0xffff3b55);c.drawRoundRect(x-ww/2,y-hh/2,x+ww/2,y+hh/2,dp(12f),dp(12f),p);p.style=Paint.Style.FILL
  p.color=color(0xffffb5bd);p.strokeWidth=dp(2.4f);p.strokeJoin=Paint.Join.ROUND
  val crack=Path().apply{moveTo(x+dp(2f),y-hh*.37f);lineTo(x-dp(5f),y-dp(22f));lineTo(x+dp(6f),y-dp(5f));lineTo(x-dp(4f),y+dp(10f));lineTo(x+dp(3f),y+hh*.36f)}
  c.drawPath(crack,p)
  p.color=0x99ff7182.toInt();c.drawLine(x-ww*.2f,y-hh*.2f,x+ww*.2f,y-hh*.29f,p)
 }
 private fun drawFighter(c:Canvas,cx:Float,cy:Float,left:Boolean){
  c.save();c.translate(cx,cy);if(!left)c.scale(-1f,1f)
  val skin=color(0xffffc18f);val ink=color(0xff251923)
  // Legs and sandals
  p.color=color(0xff174a9c);val leg=Path().apply{moveTo(-dp(20f),dp(42f));lineTo(dp(18f),dp(42f));lineTo(dp(20f),dp(99f));lineTo(dp(2f),dp(99f));lineTo(-dp(4f),dp(65f));lineTo(-dp(18f),dp(99f));lineTo(-dp(34f),dp(99f));close()};c.drawPath(leg,p)
  p.color=skin;c.drawRoundRect(-dp(38f),dp(91f),-dp(1f),dp(103f),dp(5f),dp(5f),p);c.drawRoundRect(dp(1f),dp(91f),dp(37f),dp(103f),dp(5f),dp(5f),p)
  p.color=ink;c.drawLine(-dp(30f),dp(96f),-dp(8f),dp(96f),p);c.drawLine(dp(9f),dp(96f),dp(31f),dp(96f),p)
  // Torso, angled toward the center
  p.color=if(left)color(0xffdf283d) else color(0xffff812b)
  val torso=Path().apply{moveTo(-dp(29f),-dp(22f));quadTo(-dp(12f),-dp(34f),dp(22f),-dp(25f));lineTo(dp(33f),dp(47f));quadTo(0f,dp(61f),-dp(35f),dp(43f));close()};c.drawPath(torso,p)
  p.color=if(left)color(0xffa9182e) else color(0xff202b45);p.strokeWidth=dp(3f);c.drawLine(-dp(17f),-dp(17f),-dp(10f),dp(43f),p)
  // Far arm and fist reaching the center
  p.color=skin;val arm=Path().apply{moveTo(dp(14f),-dp(18f));quadTo(dp(32f),-dp(24f),dp(43f),-dp(8f));lineTo(dp(67f),-dp(2f));quadTo(dp(76f),dp(3f),dp(70f),dp(12f));lineTo(dp(39f),dp(13f));quadTo(dp(20f),dp(14f),dp(8f),dp(5f));close()};c.drawPath(arm,p)
  p.color=color(0xffe89c72);p.strokeWidth=dp(2f);c.drawLine(dp(40f),dp(2f),dp(66f),dp(5f),p)
  // Neck and face
  p.color=skin;c.drawRoundRect(-dp(13f),-dp(49f),dp(14f),-dp(25f),dp(7f),dp(7f),p)
  p.color=skin;c.drawOval(-dp(25f),-dp(94f),dp(25f),-dp(39f),p)
  p.color=color(0xffe89b76);c.drawOval(dp(15f),-dp(69f),dp(25f),-dp(54f),p)
  // Expression: eyebrow, eye, nose, confident grin
  p.color=ink;p.strokeWidth=dp(3f);c.drawLine(dp(2f),-dp(76f),dp(17f),-dp(78f),p)
  p.color=Color.WHITE;c.drawOval(dp(3f),-dp(70f),dp(18f),-dp(59f),p)
  p.color=color(0xff25243a);c.drawCircle(dp(13f),-dp(65f),dp(3.4f),p)
  p.color=ink;p.strokeWidth=dp(2f);c.drawLine(dp(17f),-dp(53f),dp(22f),-dp(55f),p);c.drawLine(dp(3f),-dp(48f),dp(17f),-dp(47f),p)
  p.color=Color.WHITE;c.drawLine(dp(5f),-dp(48f),dp(15f),-dp(47f),p)
  if(left)drawLuffyHair(c,ink) else drawNarutoHair(c,ink)
  c.restore()
 }
 private fun drawLuffyHair(c:Canvas,ink:Int){
  p.color=color(0xff17151b)
  val hair=Path().apply{moveTo(-dp(25f),-dp(77f));lineTo(-dp(29f),-dp(96f));lineTo(-dp(13f),-dp(88f));lineTo(-dp(8f),-dp(105f));lineTo(dp(2f),-dp(90f));lineTo(dp(15f),-dp(101f));lineTo(dp(18f),-dp(84f));lineTo(dp(26f),-dp(91f));lineTo(dp(25f),-dp(72f));close()};c.drawPath(hair,p)
  // Straw hat brim and crown
  p.color=color(0xffe6a52f);c.drawOval(-dp(42f),-dp(111f),dp(42f),-dp(95f),p)
  p.color=color(0xffffc64b);c.drawRoundRect(-dp(29f),-dp(133f),dp(29f),-dp(104f),dp(12f),dp(12f),p)
  p.color=color(0xffc92f39);c.drawRect(-dp(29f),-dp(112f),dp(29f),-dp(106f),p)
  p.color=color(0xffa66b29);p.strokeWidth=dp(1.5f);c.drawArc(-dp(36f),-dp(110f),dp(36f),-dp(97f),0f,180f,false,p)
 }
 private fun drawNarutoHair(c:Canvas,ink:Int){
  p.color=color(0xffffd52f)
  val hair=Path().apply{moveTo(-dp(25f),-dp(75f));lineTo(-dp(39f),-dp(96f));lineTo(-dp(23f),-dp(91f));lineTo(-dp(28f),-dp(112f));lineTo(-dp(10f),-dp(99f));lineTo(-dp(5f),-dp(119f));lineTo(dp(7f),-dp(98f));lineTo(dp(22f),-dp(113f));lineTo(dp(20f),-dp(94f));lineTo(dp(36f),-dp(101f));lineTo(dp(25f),-dp(77f));close()};c.drawPath(hair,p)
  p.color=color(0xff252b39);c.drawRoundRect(-dp(27f),-dp(88f),dp(27f),-dp(78f),dp(3f),dp(3f),p)
  p.color=color(0xffaeb9c8);c.drawRoundRect(-dp(12f),-dp(87f),dp(12f),-dp(79f),dp(2f),dp(2f),p)
  p.color=color(0xff59677b);p.strokeWidth=dp(1.5f);c.drawLine(-dp(7f),-dp(83f),dp(7f),-dp(83f),p)
  // Three whisker marks
  p.color=color(0xffb66f57);p.strokeWidth=dp(1.6f)
  for(i in 0..2)c.drawLine(-dp(20f),-dp(62f+i*6f),-dp(10f),-dp(60f+i*6f),p)
 }
 private fun drawParticles(c:Canvas,w:Float,h:Float){
  for(i in 0..42){val x=(i*91f+tick*(i%3+1)*.42f)%w;val y=(i*137f+tick*(i%4+1)*.55f)%h
   p.color=if(i%2==0)0x77ffb39f else 0x773dbbff;c.drawCircle(x,y,dp(if(i%6==0)2.8f else 1.1f),p)}
  val pulse=dp(2f+sin(tick/8.0).toFloat()*1.2f);p.color=0x55ffffff;c.drawCircle(w/2,dp(100f),pulse,p)
 }
}
