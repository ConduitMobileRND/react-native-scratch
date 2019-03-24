
# react-native-scratch

## Getting started

`$ npm install react-native-scratch --save`

### Mostly automatic installation

`$ react-native link react-native-scratch`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-scratch` and add `RNScratch.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNScratch.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNScratchPackage;` to the imports at the top of the file
  - Add `new RNScratchPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-scratch'
  	project(':react-native-scratch').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-scratch/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-scratch')
  	```


## Usage

The ScratchView will fill its containing view and cover all other content untill you scratch it
Just put it as the last component in your view
```javascript
import ScratchView from 'react-native-scratch';

<View style={{ width: 150, height: 150 }}>
	<ComponentA> // will be covered
	<ComponentB> // will be covered
	<ScratchView
		id={1} // optional
		threshold={70} // report full scratch after 70 percentage, change as you see fit
		imageUrl="http://yourUrlToImage.jpg"
		onTouchStateChanged={this.onTouchStateChangedMethod}
		onScratchProgressChanged={this.onScratchProgressChanged}
		onScratchDone={this.onScratchDone}
		/>}
</View>
```
  