package com.livecraft

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

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
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private var tick=0;private var winResetScheduled=false
 private val d=resources.displayMetrics.density
 private val luffySprite=BitmapFactory.decodeResource(resources,R.drawable.luffy)
 private val narutoSprite=BitmapFactory.decodeResource(resources,R.drawable.naruto)
 private val giftBitmaps=ConcurrentHashMap<String,Bitmap>()
 private val giftLoading=ConcurrentHashMap.newKeySet<String>()
 private val giftImages=mapOf(
  "rose" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/eba3a9bb85c33e017f3648eaf88d7189~tplv-obj.webp",
  "tiny_diny" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/3851a489dea62b3a9fe99f61de5470ae~tplv-obj.webp",
  "paper_crane" to "https://p16-webcast.tiktokcdn.com/img/alisg/webcast-sg/0f158a08f7886189cdabf496e8a07c21~tplv-obj.webp",
  "galaxy" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/resource/79a02148079526539f7599150da9fd28.png~tplv-obj.webp",
  "white_rose" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/resource/a2d81f3847457be9083a9c76a59b08cb.png~tplv-obj.webp",
  "rosa" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/eb77ead5c3abb6da6034d3cf6cfeb438~tplv-obj.webp",
  "hat_mustache" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/2f1e4f3f5c728ffbfa35705b480fdc92~tplv-obj.webp",
  "dinosaur" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/resource/e1b0687ef6df9d6db8222831d353fcf9.png~tplv-obj.webp"
 )
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
  drawHeader(c,w,s)
  drawVillage(c,w,h)
  drawShip(c,w,h)
  val arenaTop=h*.355f;val arenaBottom=h*.655f
  drawArena(c,dp(12f),arenaTop,w-dp(12f),arenaBottom)
  drawFighter(c,w*.285f,h*.505f,true)
  drawFighter(c,w*.715f,h*.505f,false)
  drawItemPanel(c,w*.285f,h*.505f,true)
  drawItemPanel(c,w*.715f,h*.505f,false)
  drawWall(c,w/2f+s.wall.coerceIn(-1f,1f)*w*.24f,h*.505f,h)
  s.impact?.let{
   p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(30f)
   p.setShadowLayer(dp(12f),0f,0f,Color.WHITE);p.color=Color.WHITE
   c.drawText("+${it.amount}",if(it.fighter==Fighter.LUFFY)w*.285f else w*.715f,arenaTop+dp(35f),p);p.clearShadowLayer()
  }
  s.winner?.let{
   p.color=0xdd080d19.toInt();c.drawRoundRect(w*.06f,h*.30f,w*.94f,h*.70f,dp(28f),dp(28f),p)
   p.style=Paint.Style.STROKE;p.strokeWidth=dp(4f);p.color=if(it==Fighter.LUFFY)argb(0xffffc56e) else argb(0xffff8c8c)
   c.drawRoundRect(w*.06f,h*.30f,w*.94f,h*.70f,dp(28f),dp(28f),p);p.style=Paint.Style.FILL
   p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(58f);p.color=Color.WHITE
   p.setShadowLayer(dp(22f),0f,0f,if(it==Fighter.LUFFY)argb(0xffffc56e) else argb(0xffff8c8c))
   c.drawText(if(it==Fighter.LUFFY)"LUFFY" else "NARUTO",w/2f,h*.475f,p)
   p.textSize=dp(48f);c.drawText("WINS!",w/2f,h*.57f,p);p.clearShadowLayer()
   if(!winResetScheduled){winResetScheduled=true;postDelayed({GameState.finishRound();winResetScheduled=false},5000L)}
  }
  drawParticles(c,w,h)
  postInvalidateDelayed(45)
 }
 private fun drawHeader(c:Canvas,w:Float,s:BattleSnapshot){
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(27f);p.color=Color.WHITE
  p.setShadowLayer(dp(8f),0f,dp(2f),Color.BLACK);c.drawText("PUSH BATTLE",w/2,dp(43f),p);p.clearShadowLayer()
  p.textSize=dp(13f);p.color=argb(0xffffd083);c.drawText("ANIME DUEL",w/2,dp(64f),p)
  drawWinsBadge(c,w*.285f,dp(170f),s.luffyWins,true)
  drawWinsBadge(c,w*.715f,dp(170f),s.narutoWins,false)
 }
 private fun drawWinsBadge(c:Canvas,x:Float,y:Float,count:Int,left:Boolean){
  val label="WINS $count"
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(14f)
  val color=if(left)argb(0xffffc56e) else argb(0xffff8c8c)
  val half=dp(43f)
  p.color=0xdd101827.toInt();c.drawRoundRect(x-half,y-dp(12f),x+half,y+dp(12f),dp(10f),dp(10f),p)
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(1.5f);p.color=color
  c.drawRoundRect(x-half,y-dp(12f),x+half,y+dp(12f),dp(10f),dp(10f),p);p.style=Paint.Style.FILL
  p.color=Color.WHITE;c.drawText(label,x,y+dp(5f),p)
 }
 private fun drawVillage(c:Canvas,w:Float,h:Float){
  val top=dp(78f);val bottom=h*.345f
  p.shader=LinearGradient(0f,top,0f,bottom,argb(0xff68b9e5),argb(0xffffdda0),Shader.TileMode.CLAMP);c.drawRect(0f,top,w,bottom,p);p.shader=null
  p.color=argb(0xffffedb0);c.drawCircle(w*.82f,top+dp(38f),dp(19f),p)
  // distant mountain silhouettes
  p.color=argb(0xff668b83)
  val far=Path().apply{moveTo(0f,bottom-dp(48f));lineTo(w*.18f,bottom-dp(112f));lineTo(w*.34f,bottom-dp(53f));lineTo(w*.58f,bottom-dp(124f));lineTo(w*.82f,bottom-dp(48f));lineTo(w,bottom-dp(90f));lineTo(w,bottom);lineTo(0f,bottom);close()};c.drawPath(far,p)
  p.color=argb(0xff477b57)
  val near=Path().apply{moveTo(0f,bottom-dp(26f));quadTo(w*.25f,bottom-dp(83f),w*.48f,bottom-dp(29f));quadTo(w*.76f,bottom-dp(88f),w,bottom-dp(24f));lineTo(w,bottom);lineTo(0f,bottom);close()};c.drawPath(near,p)
  // village street
  p.color=argb(0xffc6a477);c.drawOval(w*.25f,bottom-dp(24f),w*.78f,bottom+dp(12f),p)
  drawHouse(c,w*.17f,bottom-dp(13f),w*.24f,dp(78f),argb(0xfff0d6a5),argb(0xff8f4438))
  drawHouse(c,w*.49f,bottom-dp(2f),w*.29f,dp(104f),argb(0xffffe6bd),argb(0xffa94c3c))
  drawHouse(c,w*.83f,bottom-dp(13f),w*.22f,dp(72f),argb(0xffe8c99b),argb(0xff75483b))
  // trees
  for(x in listOf(w*.035f,w*.34f,w*.69f,w*.97f)){
   p.color=argb(0xff62452f);c.drawRoundRect(x-dp(3f),bottom-dp(45f),x+dp(3f),bottom,dp(3f),dp(3f),p)
   p.color=argb(0xff276344);c.drawCircle(x,bottom-dp(54f),dp(17f),p)
   p.color=argb(0xff438b4c);c.drawCircle(x-dp(8f),bottom-dp(62f),dp(12f),p)
   p.color=argb(0xff65a957);c.drawCircle(x+dp(8f),bottom-dp(60f),dp(10f),p)
  }
 }
 private fun drawHouse(c:Canvas,cx:Float,base:Float,houseW:Float,houseH:Float,wall:Int,roof:Int){
  val left=cx-houseW/2;val right=cx+houseW/2;val top=base-houseH
  p.color=argb(0x55332218);c.drawRoundRect(left-dp(4f),top+houseH*.28f,right+dp(4f),base+dp(3f),dp(4f),dp(4f),p)
  p.color=wall;c.drawRect(left,top+houseH*.3f,right,base,p)
  p.color=argb(0xffd7b887);c.drawRect(left+dp(4f),top+houseH*.34f,right-dp(4f),top+houseH*.39f,p)
  p.color=roof
  val r=Path().apply{moveTo(left-dp(10f),top+houseH*.34f);lineTo(cx,top-dp(5f));lineTo(right+dp(10f),top+houseH*.34f);lineTo(right-dp(2f),top+houseH*.39f);lineTo(cx,top+dp(7f));lineTo(left+dp(2f),top+houseH*.39f);close()};c.drawPath(r,p)
  p.color=argb(0xff593b32);c.drawRect(cx-dp(10f),base-dp(34f),cx+dp(10f),base,p)
  p.color=argb(0xff8ed6e5);c.drawRoundRect(left+dp(9f),top+houseH*.48f,left+dp(25f),top+houseH*.68f,dp(2f),dp(2f),p)
  c.drawRoundRect(right-dp(25f),top+houseH*.48f,right-dp(9f),top+houseH*.68f,dp(2f),dp(2f),p)
  p.color=argb(0xfff4dfad);p.strokeWidth=dp(1.5f)
  c.drawLine(left+dp(17f),top+houseH*.48f,left+dp(17f),top+houseH*.68f,p)
  c.drawLine(right-dp(17f),top+houseH*.48f,right-dp(17f),top+houseH*.68f,p)
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
  val ww=dp(34f);val hh=h*.16f;val l=x-ww/2;val r=x+ww/2;val t=y-hh/2;val b=y+hh/2
  glow(c,x,y,dp(78f),argb(0x8899dfff))
  // stone core and metal frame
  p.setShadowLayer(dp(14f),0f,dp(3f),argb(0xcc000000))
  p.shader=LinearGradient(l,t,r,b,argb(0xffd9e0e8),argb(0xff596b80),Shader.TileMode.CLAMP)
  c.drawRoundRect(l,t,r,b,dp(8f),dp(8f),p);p.shader=null;p.clearShadowLayer()
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(3f);p.color=argb(0xfff0c878);c.drawRoundRect(l,t,r,b,dp(8f),dp(8f),p);p.style=Paint.Style.FILL
  // stone blocks
  p.color=argb(0xff8495a8);p.strokeWidth=dp(1.3f)
  for(i in 1..4){val yy=t+hh*i/5f;c.drawLine(l+dp(3f),yy,r-dp(3f),yy,p)}
  c.drawLine(x,t+dp(4f),x,b-dp(4f),p)
  // glowing central crystal
  glow(c,x,y,dp(27f),argb(0xff70d9ff))
  val crystal=Path().apply{moveTo(x,y-dp(22f));lineTo(x+dp(11f),y-dp(5f));lineTo(x+dp(7f),y+dp(17f));lineTo(x,y+dp(23f));lineTo(x-dp(8f),y+dp(8f));lineTo(x-dp(10f),y-dp(7f));close()}
  p.shader=LinearGradient(x-dp(10f),y-dp(20f),x+dp(10f),y+dp(20f),argb(0xffe5fbff),argb(0xff37a9e8),Shader.TileMode.CLAMP);c.drawPath(crystal,p);p.shader=null
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(1.5f);p.color=Color.WHITE;c.drawPath(crystal,p);p.style=Paint.Style.FILL
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
 private fun drawItemPanel(c:Canvas,x:Float,y:Float,left:Boolean){
  val ids=if(left) listOf("rose","tiny_diny","paper_crane","galaxy") else listOf("white_rose","rosa","hat_mustache","dinosaur")
  val icon=dp(29f);val gap=dp(5f);val px=x-icon/2f;val py=y+dp(110f)
  val values=listOf("+1","+10","+100","+1000")
  ids.forEachIndexed{index,id->
   val yy=py+index*(icon+gap)
   p.color=0xdd101827.toInt();c.drawRoundRect(px-dp(4f),yy-dp(2f),px+icon+dp(48f),yy+icon+dp(2f),dp(8f),dp(8f),p)
   p.style=Paint.Style.STROKE;p.strokeWidth=dp(1f);p.color=if(left)argb(0xffffc56e) else argb(0xffff8c8c)
   c.drawRoundRect(px-dp(4f),yy-dp(2f),px+icon+dp(48f),yy+icon+dp(2f),dp(8f),dp(8f),p);p.style=Paint.Style.FILL
   val bitmap=giftBitmaps[id]
   if(bitmap!=null){p.alpha=255;p.isFilterBitmap=true;c.drawBitmap(bitmap,null,RectF(px,yy,px+icon,yy+icon),p);p.isFilterBitmap=false}else loadGiftImage(id)
   p.textAlign=Paint.Align.LEFT;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(13f)
   p.color=Color.WHITE;p.setShadowLayer(dp(3f),0f,dp(1f),Color.BLACK)
   c.drawText(values[index],px+icon+dp(8f),yy+dp(20f),p);p.clearShadowLayer()
  }
 }
 private fun loadGiftImage(id:String){
  val imageUrl=giftImages[id]?:return
  if(!giftLoading.add(id))return
  Thread{
   try{val connection=URL(imageUrl).openConnection();connection.connectTimeout=8000;connection.readTimeout=8000;connection.setRequestProperty("User-Agent","Mozilla/5.0");val bitmap=connection.getInputStream().use{BitmapFactory.decodeStream(it)};if(bitmap!=null)giftBitmaps[id]=bitmap}
   catch(e:Exception){android.util.Log.w("AnimePushBattle","Não foi possível carregar ícone "+id,e)}
   finally{giftLoading.remove(id);postInvalidate()}
  }.start()
 }
 private fun drawShip(c:Canvas,w:Float,h:Float){
  val top=h*.68f;val bottom=h*.96f
  p.shader=LinearGradient(0f,top,0f,bottom,argb(0xff8ed9ed),argb(0xff07517d),Shader.TileMode.CLAMP);c.drawRect(0f,top,w,bottom,p);p.shader=null
  // layered ocean
  for(i in 0..3){
   val yy=top+dp(48f+i*27f);p.color=if(i%2==0)argb(0xff258eb2) else argb(0xff14749c)
   val wave=Path().apply{moveTo(0f,yy);quadTo(w*.22f,yy-dp(13f),w*.48f,yy);quadTo(w*.76f,yy+dp(12f),w,yy);lineTo(w,bottom);lineTo(0f,bottom);close()};c.drawPath(wave,p)
  }
  val deckY=top+dp(112f)
  // ship shadow
  p.color=argb(0x55001d35);c.drawOval(w*.12f,deckY+dp(28f),w*.9f,deckY+dp(65f),p)
  // deep wooden hull
  val hull=Path().apply{moveTo(w*.12f,deckY);lineTo(w*.88f,deckY);lineTo(w*.78f,top+dp(183f));quadTo(w*.5f,top+dp(218f),w*.22f,top+dp(183f));close()}
  p.shader=LinearGradient(0f,deckY,0f,top+dp(210f),argb(0xffa65d32),argb(0xff4b2924),Shader.TileMode.CLAMP);c.drawPath(hull,p);p.shader=null
  p.color=argb(0xffe1a45c);p.strokeWidth=dp(5f);c.drawLine(w*.13f,deckY,w*.87f,deckY,p)
  p.color=argb(0xffd18a4c);p.strokeWidth=dp(2f)
  for(i in 0..6){val xx=w*(.22f+i*.093f);c.drawLine(xx,deckY+dp(8f),xx-dp(4f),top+dp(184f),p)}
  // deck rail
  p.color=argb(0xff57352a);p.strokeWidth=dp(3f);c.drawLine(w*.2f,deckY-dp(10f),w*.8f,deckY-dp(10f),p)
  for(i in 0..7){val xx=w*(.22f+i*.08f);c.drawLine(xx,deckY-dp(10f),xx,deckY+dp(2f),p)}
  // mast and crossbeam
  val mx=w*.5f;val mastTop=top+dp(8f)
  p.color=argb(0xff503326);p.strokeWidth=dp(7f);c.drawLine(mx,mastTop,mx,deckY+dp(1f),p)
  c.drawLine(w*.29f,top+dp(76f),w*.72f,top+dp(76f),p)
  // two cloth sails
  p.color=argb(0xfffff0cf)
  val sail=Path().apply{moveTo(mx-dp(5f),top+dp(19f));lineTo(w*.31f,top+dp(73f));lineTo(mx-dp(5f),top+dp(73f));close()};c.drawPath(sail,p)
  val sail2=Path().apply{moveTo(mx+dp(5f),top+dp(25f));lineTo(w*.7f,top+dp(73f));lineTo(mx+dp(5f),top+dp(73f));close()};c.drawPath(sail2,p)
  p.color=argb(0xffd9b77f);p.strokeWidth=dp(1.5f);c.drawLine(mx-dp(5f),top+dp(19f),mx-dp(5f),top+dp(73f),p);c.drawLine(mx+dp(5f),top+dp(25f),mx+dp(5f),top+dp(73f),p)
  // pirate flag
  p.color=argb(0xff282c38);val flag=Path().apply{moveTo(mx, mastTop+dp(3f));lineTo(mx+dp(29f),mastTop+dp(8f));lineTo(mx,mastTop+dp(23f));close()};c.drawPath(flag,p)
  p.color=argb(0xfff0c878);c.drawCircle(mx,mastTop+dp(4f),dp(3f),p)
  // portholes
  p.color=argb(0xffffd17a);for(i in 0..2)c.drawCircle(w*(.39f+i*.11f),top+dp(170f),dp(4f),p)
 }
 private fun drawParticles(c:Canvas,w:Float,h:Float){
  for(i in 0..24){val x=((i*73+tick*2)%w.toInt().coerceAtLeast(1)).toFloat();val y=((i*137+tick*3)%(h.toInt().coerceAtLeast(1))).toFloat();p.color=if(i%2==0)0x55ffb36b else 0x554dbbff;c.drawCircle(x,y,dp(if(i%5==0)2.5f else 1f),p)}
 }
}
