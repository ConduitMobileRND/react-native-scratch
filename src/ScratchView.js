// ComoScratchImageView.js
import React, { Component } from 'react';
import { StyleSheet, Animated, requireNativeComponent } from 'react-native';

const RNTScratchView = requireNativeComponent('RNTScratchView', ScratchView);

class ScratchView extends Component {
    constructor(props) {
        super(props);

        this.state = {
            animatedValue: new Animated.Value(1),
            visible: true,
        };

        this.scratchOpacity = {
            opacity: this.state.animatedValue.interpolate({
                inputRange: [0, 1],
                outputRange: [1, 0],
            }),
        };
    }

    onLayout = (e) => {
        const { width, height } = e.nativeEvent.layout;
        this.setState({ width, height })
    };

    onImageLoadFinished = (e) => {
        this.props.onImageLoadFinished && e.nativeEvent.state && this.props.onImageLoadFinished({ id: this.props.id, touchState: JSON.parse(e.nativeEvent.success) });
    }

    onTouchStateChanged = (e) => {
        this.props.onTouchStateChanged && e.nativeEvent.state && this.props.onTouchStateChanged({ id: this.props.id, touchState: JSON.parse(e.nativeEvent.state) });
    }

    onScratchProgressChanged = (e) => {
        this.props.onScratchChanged && e.nativeEvent.value && this.props.onScratchChanged({ id: this.props.id, value: parseFloat(e.nativeEvent.value) });
    }

    onScratchDone = (e) => {
        const isDone = JSON.parse(e.nativeEvent.isDone);
        if (isDone) {
            this.fadeOut();
        }
        this.props.onScratchDone && e.nativeEvent.isDone && this.props.onScratchDone({ id: this.props.id, isDone });
    }

    fadeOut() {
        this.state.animatedValue.setValue(1);
        Animated.timing(this.state.animatedValue, {
            toValue: 0,
            duration: 300,
            useNativeDriver: true,
        }).start(() => { this.setState({ visible: false }); this.props.onTouchStateChanged({ id: this.props.id, touchState: false }) });
    }

    render() {
        if (this.state.visible) {
            return (
                <Animated.View style={[styles.container, { opacity: this.state.animatedValue }]} onLayout={this.onLayout}>
                    <RNTScratchView
                        {...this.props}
                        style={{ width: '100%', height: '100%' }}
                        ref={(ref) => this.ref = ref}
                        onImageLoadFinished={this.onImageLoadFinished}
                        onTouchStateChanged={this.onTouchStateChanged}
                        onScratchProgressChanged={this.onScratchProgressChanged}
                        onScratchDone={this.onScratchDone}
                    />
                </Animated.View>
            );
        }
        return null;
    }
}


const styles = StyleSheet.create({
    container: {
        position: 'absolute',
        width: '100%',
        height: '100%'
    },
});

export default ScratchView

