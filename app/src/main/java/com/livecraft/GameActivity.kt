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
 private val luffySprite=BitmapFactory.decodeResource(resources,R.drawable.luffy)
 private val narutoSprite=BitmapFactory.decodeResource(resources,R.drawable.naruto)
 private fun dp(v:Float)=v*d
 private fun argb(v:Long)=v.toInt()
 init{GameState.onChanged={postInvalidate()}}
 override fun onDraw(c:Canvas){
  try { drawFrame(c) } catch (e:RuntimeException) {
   p.shader=null;p.clearShadowLayer();p.style=Paint.Style.FILL;p.color=Color.rgb(12,14,28);c.drawColor(p.color)
   p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.textSize=dp(16f);p.typeface=Typeface.DEFAULT_BOLD
   c.drawText("ERRO NA ARENA",width/2f,height/2f-dp(12f),p)
   p.textSize=dp(11f);p.typeface=Typeface.DEFAULT
   val msg=e.message?.take(48) ?: e.javaClass.simpleName
   c.drawText(msg,width/2f,height/2f+dp(14f),p)
   android.util.Log.e("AnimePushBattle","Falha ao desenhar arena",e)
  }
 }
 private fun drawFrame(c:Canvas){
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
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(2f);p.color=edge;c.drawRoundRect(l,t,r,b,dp(22f),dp(22f),p);p.style=Paint.Style.FILL
  // Faint diagonal energy streaks give each side its own atmosphere.
  for(i in 0..7){val yy=t+dp(45f+i*105f);p.color=(if(left)0x18ffb18a else 0x183ecaff).toInt();p.strokeWidth=dp(2f);c.drawLine(l+dp(12f),yy,r-dp(12f),yy-dp(30f),p)}
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
  val bitmap=if(left)luffySprite else narutoSprite
  val targetHeight=dp(155f)
  val targetWidth=targetHeight*bitmap.width/bitmap.height
  val dst=RectF(x-targetWidth/2f,y-dp(83f),x+targetWidth/2f,y+dp(72f))
  p.alpha=255;p.isFilterBitmap=true
  c.drawBitmap(bitmap,null,dst,p)
  p.isFilterBitmap=false
  p.textAlign=Paint.Align.CENTER;p.textSize=dp(18f);p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.color=Color.WHITE
  c.drawText(if(left)"LUFFY" else "NARUTO",x,y+dp(124f),p)
 }
 private fun drawParticles(c:Canvas,w:Float,h:Float){
  for(i in 0..24){val x=((i*73+tick*2)%w.toInt().coerceAtLeast(1)).toFloat();val y=((i*137+tick*3)%(h.toInt().coerceAtLeast(1))).toFloat();p.color=if(i%2==0)0x55ffb36b else 0x554dbbff;p.color=p.color;c.drawCircle(x,y,dp(if(i%5==0)2.5f else 1f),p)}
 }

}