import {useState,useEffect,useRef} from 'react';import {Text,View} from 'react-native';import Storage from 'expo-sqlite/kv-store';import {Local,initial,Data} from '../domain/local';
export function LocalProvider({children}:{children:React.ReactNode}){const [data,setData]=useState(initial),[ready,setReady]=useState(false),[error,setError]=useState(false);const current=useRef(initial);
useEffect(()=>{Storage.getItem('nomi-preview-v1').then(raw=>{if(raw){const d={...initial,...JSON.parse(raw)} as Data;current.current=d;setData(d)}setReady(true)}).catch(()=>setError(true));},[]);
function update(fn:(d:Data)=>Data){const next=fn(current.current);Storage.setItemSync('nomi-preview-v1',JSON.stringify(next));current.current=next;setData(next)}
if(!ready)return <View style={{flex:1,justifyContent:'center',alignItems:'center'}}><Text>{error?'تعذر فتح التخزين المحلي. لا تحذف البيانات.':'جارٍ تحميل نومي...'}</Text></View>;
return <Local.Provider value={{data,update}}>{children}</Local.Provider>}
