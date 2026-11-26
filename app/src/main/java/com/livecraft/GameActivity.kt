package com.livecraft

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager

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
  p.shader=LinearGradient(0f,0f,w,h,argb(0xff101c35),argb(0xff183c53),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  drawHeader(c,w)
  drawVillage(c,w,h)
  drawShip(c,w,h)
  val arenaTop=h*.355f;val arenaBottom=h*.655f
  drawArena(c,dp(12f),arenaTop,w-dp(12f),arenaBottom)
  drawFighter(c,w*.285f,h*.505f,true)
  drawFighter(c,w*.715f,h*.505f,false)
  drawWall(c,w/2f+s.wall.coerceIn(-1f,1f)*w*.24f,h*.505f,h)
  s.impact?.let{
   p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(30f)
   p.setShadowLayer(dp(12f),0f,0f,Color.WHITE);p.color=Color.WHITE
   c.drawText("+${it.amount}",if(it.fighter==Fighter.LUFFY)w*.285f else w*.715f,arenaTop+dp(35f),p);p.clearShadowLayer()
  }
  s.winner?.let{p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=dp(32f);p.color=Color.WHITE;p.setShadowLayer(dp(18f),0f,0f,Color.YELLOW);c.drawText(if(it==Fighter.LUFFY)"LUFFY WIN!" else "NARUTO WIN!",w/2,h*.735f,p);p.clearShadowLayer()}
  drawParticles(c,w,h)
  postInvalidateDelayed(45)
 }
 private fun drawHeader(c:Canvas,w:Float){
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(27f);p.color=Color.WHITE
  p.setShadowLayer(dp(8f),0f,dp(2f),Color.BLACK);c.drawText("PUSH BATTLE",w/2,dp(43f),p);p.clearShadowLayer()
  p.textSize=dp(13f);p.color=argb(0xffffd083);c.drawText("ANIME DUEL",w/2,dp(64f),p)
 }
 private fun drawVillage(c:Canvas,w:Float,h:Float){
  val top=dp(78f);val bottom=h*.345f
  p.shader=LinearGradient(0f,top,0f,bottom,argb(0xff75c8e8),argb(0xffd5e8b1),Shader.TileMode.CLAMP);c.drawRect(0f,top,w,bottom,p);p.shader=null
  // distant hills
  p.color=argb(0xff6baf79);val hills=Path().apply{moveTo(0f,bottom-dp(28f));quadTo(w*.22f,bottom-dp(105f),w*.48f,bottom-dp(35f));quadTo(w*.76f,bottom-dp(115f),w,bottom-dp(30f));lineTo(w,bottom);lineTo(0f,bottom);close()};c.drawPath(hills,p)
  // village houses
  drawHouse(c,w*.16f,bottom-dp(18f),w*.25f,dp(82f),argb(0xfff2d6a0),argb(0xff9c4b37))
  drawHouse(c,w*.49f,bottom-dp(8f),w*.27f,dp(105f),argb(0xffffe2ad),argb(0xffa84435))
  drawHouse(c,w*.82f,bottom-dp(17f),w*.22f,dp(76f),argb(0xffe8c58d),argb(0xff754333))
  // trees and foliage
  for(x in listOf(w*.035f,w*.34f,w*.68f,w*.96f)){
   p.color=argb(0xff68452e);c.drawRect(x-dp(4f),bottom-dp(65f),x+dp(4f),bottom,p)
   p.color=argb(0xff2f804c);c.drawCircle(x,bottom-dp(73f),dp(22f),p)
   p.color=argb(0xff55a95c);c.drawCircle(x-dp(10f),bottom-dp(81f),dp(13f),p)
  }
  p.color=argb(0xfff5e4b8);p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(10f);c.drawText("VILA",w/2,bottom-dp(5f),p)
 }
 private fun drawHouse(c:Canvas,cx:Float,base:Float,houseW:Float,houseH:Float,wall:Int,roof:Int){
  val left=cx-houseW/2;val right=cx+houseW/2;val top=base-houseH
  p.color=wall;c.drawRect(left,top+houseH*.3f,right,base,p)
  p.color=roof;val r=Path().apply{moveTo(left-dp(8f),top+houseH*.34f);lineTo(cx,top);lineTo(right+dp(8f),top+houseH*.34f);close()};c.drawPath(r,p)
  p.color=argb(0xff593b32);c.drawRect(cx-dp(9f),base-dp(32f),cx+dp(9f),base,p)
  p.color=argb(0xff8ed0df);c.drawRect(left+dp(9f),top+houseH*.48f,left+dp(25f),top+houseH*.68f,p)
  c.drawRect(right-dp(25f),top+houseH*.48f,right-dp(9f),top+houseH*.68f,p)
 }
 private fun drawArena(c:Canvas,l:Float,t:Float,r:Float,b:Float){
  p.setShadowLayer(dp(18f),0f,dp(5f),argb(0xaa000000))
  p.shader=LinearGradient(l,t,r,b,argb(0xff272b43),argb(0xff101522),Shader.TileMode.CLAMP)
  c.drawRoundRect(l,t,r,b,dp(24f),dp(24f),p);p.shader=null;p.clearShadowLayer()
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(3f);p.color=argb(0xffe8c47a);c.drawRoundRect(l,t,r,b,dp(24f),dp(24f),p);p.style=Paint.Style.FILL
  // central fighting lane
  p.color=argb(0x22ffffff);c.drawRoundRect(l+dp(12f),t+dp(12f),r-dp(12f),b-dp(12f),dp(16f),dp(16f),p)
  p.color=argb(0x55ffffff);p.strokeWidth=dp(1f);c.drawLine((l+r)/2,t+dp(18f),(l+r)/2,b-dp(18f),p)
 }
 private fun glow(c:Canvas,x:Float,y:Float,r:Float,col:Int){
  p.shader=RadialGradient(x,y,r,col,0x00000000,Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null
 }
 private fun drawWall(c:Canvas,x:Float,y:Float,h:Float){
  val ww=dp(27f);val hh=h*.105f
  glow(c,x,y,dp(85f),argb(0xffff203c))
  p.setShadowLayer(dp(12f),0f,0f,argb(0xffff1736));p.shader=LinearGradient(x-ww/2,y-hh/2,x+ww/2,y+hh/2,argb(0xffa41630),argb(0xff3e071b),Shader.TileMode.CLAMP)
  c.drawRoundRect(x-ww/2,y-hh/2,x+ww/2,y+hh/2,dp(9f),dp(9f),p);p.shader=null;p.clearShadowLayer()
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(3f);p.color=argb(0xffff3b55);c.drawRoundRect(x-ww/2,y-hh/2,x+ww/2,y+hh/2,dp(9f),dp(9f),p);p.style=Paint.Style.FILL
  p.color=argb(0xffffb5bd);p.strokeWidth=dp(2f)
  val crack=Path().apply{moveTo(x+dp(1f),y-hh*.35f);lineTo(x-dp(5f),y-dp(10f));lineTo(x+dp(5f),y);lineTo(x-dp(3f),y+dp(10f));lineTo(x+dp(2f),y+hh*.35f)}
  c.drawPath(crack,p)
 }
 private fun drawFighter(c:Canvas,x:Float,y:Float,left:Boolean){
  val bitmap=if(left)luffySprite else narutoSprite
  val targetHeight=dp(150f)
  val targetWidth=targetHeight*bitmap.width/bitmap.height
  val dst=RectF(x-targetWidth/2f,y-dp(76f),x+targetWidth/2f,y+dp(74f))
  p.alpha=255;p.isFilterBitmap=true;c.drawBitmap(bitmap,null,dst,p);p.isFilterBitmap=false
  p.textAlign=Paint.Align.CENTER;p.textSize=dp(17f);p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.color=Color.WHITE
  p.setShadowLayer(dp(5f),0f,dp(2f),Color.BLACK);c.drawText(if(left)"LUFFY" else "NARUTO",x,y+dp(99f),p);p.clearShadowLayer()
 }
 private fun drawShip(c:Canvas,w:Float,h:Float){
  val top=h*.68f;val bottom=h*.96f
  p.shader=LinearGradient(0f,top,0f,bottom,argb(0xff79c9e8),argb(0xff07517d),Shader.TileMode.CLAMP);c.drawRect(0f,top,w,bottom,p);p.shader=null
  // rolling sea
  p.color=argb(0xff1683ad)
  for(i in 0..3){val yy=top+dp(45f+i*28f);val wave=Path().apply{moveTo(0f,yy);quadTo(w*.25f,yy-dp(16f),w*.5f,yy);quadTo(w*.75f,yy+dp(15f),w,yy);lineTo(w,bottom);lineTo(0f,bottom);close()};c.drawPath(wave,p)}
  // ship hull
  val deckY=top+dp(75f)
  p.color=argb(0xff633b2d);val hull=Path().apply{moveTo(w*.13f,deckY);lineTo(w*.87f,deckY);lineTo(w*.76f,top+dp(142f));quadTo(w*.5f,top+dp(176f),w*.24f,top+dp(142f));close()};c.drawPath(hull,p)
  p.color=argb(0xffb87945);p.strokeWidth=dp(5f);c.drawLine(w*.17f,deckY,w*.83f,deckY,p)
  for(i in 0..5){val xx=w*(.25f+i*.1f);p.color=argb(0xffd49a5b);p.strokeWidth=dp(2f);c.drawLine(xx,deckY+dp(7f),xx-dp(4f),top+dp(143f),p)}
  // mast and sails
  val mastX=w*.5f;p.color=argb(0xff4a3026);p.strokeWidth=dp(7f);c.drawLine(mastX,top+dp(12f),mastX,deckY+dp(3f),p)
  p.color=argb(0xfffff0d2);val sail=Path().apply{moveTo(mastX-dp(5f),top+dp(18f));lineTo(w*.27f,top+dp(65f));lineTo(mastX-dp(5f),top+dp(65f));close()};c.drawPath(sail,p)
  val sail2=Path().apply{moveTo(mastX+dp(5f),top+dp(29f));lineTo(w*.75f,top+dp(70f));lineTo(mastX+dp(5f),top+dp(70f));close()};c.drawPath(sail2,p)
  p.color=argb(0xffd94b38);c.drawCircle(mastX,top+dp(44f),dp(12f),p)
  p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(9f);c.drawText("NAVIO",w/2,bottom-dp(4f),p)
 }
 private fun drawParticles(c:Canvas,w:Float,h:Float){
  for(i in 0..24){val x=((i*73+tick*2)%w.toInt().coerceAtLeast(1)).toFloat();val y=((i*137+tick*3)%(h.toInt().coerceAtLeast(1))).toFloat();p.color=if(i%2==0)0x55ffb36b else 0x554dbbff;c.drawCircle(x,y,dp(if(i%5==0)2.5f else 1f),p)}
 }
}
