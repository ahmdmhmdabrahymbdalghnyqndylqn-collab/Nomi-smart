import 'react-native-url-polyfill/auto';
import * as SecureStore from 'expo-secure-store';
import {createClient} from '@supabase/supabase-js';
const url=process.env.EXPO_PUBLIC_SUPABASE_URL;
const key=process.env.EXPO_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
const storage={getItem:(k:string)=>SecureStore.getItemAsync(k),setItem:(k:string,v:string)=>SecureStore.setItemAsync(k,v),removeItem:(k:string)=>SecureStore.deleteItemAsync(k)};
export const supabase=url&&key?createClient(url,key,{auth:{storage,persistSession:true,autoRefreshToken:true,detectSessionInUrl:false,flowType:'pkce'}}):null;
