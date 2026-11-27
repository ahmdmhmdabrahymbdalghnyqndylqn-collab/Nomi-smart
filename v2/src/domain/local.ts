import {createContext,useContext} from 'react';
import type {Currency} from './format';
export type Task={id:string;title:string;day:string;time:string;kind:'task'|'appointment';category:'personal'|'work'|'study';done:boolean;deleted?:boolean;notifications:string[]};
export type Entry={id:string;kind:'income'|'expense';minor:number;currency:Currency;note:string;date:string};
export type Alarm={id:string;label:string;at:string;notification:string};
export type Data={country:'EG'|'JO';tasks:Task[];entries:Entry[];alarms:Alarm[];theme:'light'|'dark';variant:'green'|'purple';sound:boolean};
export const initial:Data={country:'JO',tasks:[],entries:[],alarms:[],theme:'light',variant:'green',sound:true};
export const Local=createContext<{data:Data;update:(fn:(d:Data)=>Data)=>void}>({data:initial,update:()=>{throw Error('Missing provider')}});
export const useLocal=()=>useContext(Local);
export function localDay(d=new Date()){return [d.getFullYear(),String(d.getMonth()+1).padStart(2,'0'),String(d.getDate()).padStart(2,'0')].join('-')}
export function dateTime(day:string,time:string){if(!/^\d{4}-\d{2}-\d{2}$/.test(day)||!/^([01]\d|2[0-3]):[0-5]\d$/.test(time))throw Error('Invalid date');const d=new Date(day+'T'+time+':00');if(!Number.isFinite(d.getTime())||localDay(d)!==day)throw Error('Invalid date');return d;}
