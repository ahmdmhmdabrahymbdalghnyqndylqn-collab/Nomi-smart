package qa.nomi;
import android.app.*;
import android.os.*;
import android.view.accessibility.*;
import java.util.*;
public class Check extends Instrumentation {
 Bundle args;
 public void onCreate(Bundle b){super.onCreate(b);args=b;start();}
 AccessibilityNodeInfo find(AccessibilityNodeInfo n,String label,String cls,int[] skip){
  if(n==null)return null;
  if(n.isVisibleToUser() && (cls==null||cls.contentEquals(n.getClassName())) &&
    (label==null||label.equals(String.valueOf(n.getText()))||label.equals(String.valueOf(n.getContentDescription())))){
   if(skip[0]--==0)return n;
  }
  for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo v=find(n.getChild(i),label,cls,skip);if(v!=null)return v;}return null;
 }
 AccessibilityNodeInfo node(String label,String cls,int index){
  for(int i=0;i<50;i++){AccessibilityNodeInfo n=find(getUiAutomation().getRootInActiveWindow(),label,cls,new int[]{index});if(n!=null)return n;SystemClock.sleep(200);}
  throw new AssertionError("Missing "+label+" "+cls+" "+index);
 }
 void click(String text){AccessibilityNodeInfo n=node(text,null,0);while(n!=null&&!n.performAction(AccessibilityNodeInfo.ACTION_CLICK))n=n.getParent();if(n==null)throw new AssertionError("click "+text);SystemClock.sleep(650);}
 void edit(int i,String value){AccessibilityNodeInfo n=node(null,"android.widget.EditText",i);Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value);if(!n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b))throw new AssertionError("edit "+i);}
 void dump(AccessibilityNodeInfo n,StringBuilder out){if(n==null)return; if(n.getText()!=null)out.append(n.getText()).append("|");for(int i=0;i<n.getChildCount();i++)dump(n.getChild(i),out);}
 String details(){StringBuilder b=new StringBuilder();dump(getUiAutomation().getRootInActiveWindow(),b);return b.toString();}
 String row(){node("NomiPayment",null,0);String s=details();int i=s.indexOf("NomiPayment");String tail=s.substring(i);String[] parts=tail.split("\\|");if(parts.length<3)throw new AssertionError(tail);String result=parts[0]+"|"+parts[1]+"|"+parts[2];if(!result.matches(".*(42|٤٢).*"))throw new AssertionError(result);return result;}
 void task(String name){click("المهام");edit(0,name);click("إضافة مهمة");node(name,null,0);}
 public void onStart(){Bundle r=new Bundle();try{
 if(args.getString("phase").equals("before")){
 edit(0,"Nomi QA");edit(1,"0770000000");click("اختر الدولة");click("الأردن");click("الدخول");
 task("NomiBefore");click("إنجاز");if(!node("إنجاز",null,0).isChecked())throw new AssertionError("completion");
 click("المال");int index=0;for(int i=0;i<6;i++){AccessibilityNodeInfo n=find(getUiAutomation().getRootInActiveWindow(),null,"android.widget.EditText",new int[]{i});if(n==null)break;if(n.getText()==null||n.getText().length()==0){edit(i,index++==0?"NomiPayment":"42");}}
 if(index!=2)throw new AssertionError("money fields "+index);click("إضافة حركة");r.putString("ledger",row());
 }else{click("المهام");node("NomiBefore",null,0);if(!node("إنجاز",null,0).isChecked())throw new AssertionError("lost completion");click("المال");r.putString("ledger",row());task("NomiAfter");}
 r.putString("result","PASS");finish(-1,r);
 }catch(Throwable e){r.putString("error",e.toString()+" UI="+details());finish(0,r);}}
}