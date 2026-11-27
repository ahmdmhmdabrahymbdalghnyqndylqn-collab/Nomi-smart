import {Stack} from 'expo-router';import {DesignProvider} from '../src/ui/Design';
export default function Layout(){return <DesignProvider><Stack screenOptions={{headerShown:false}}/></DesignProvider>}
