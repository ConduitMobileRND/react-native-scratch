#import <UIKit/UIKit.h>
#import <React/RCTComponent.h>
#import <React/RCTEventDispatcher.h>
#import "ScratchViewDelegate.h"

@interface ScratchView : UIImageView
{
  NSString *imageUrl;
  NSString *resourceName;
  NSString *resizeMode;
  UIColor *placeholderColor;
  float threshold;
  float brushSize;
  UIImage *image;
  UIBezierPath *path;
  float minDimension;
  float gridSize;
  NSMutableArray *grid;
  bool cleared;
  int clearPointsCounter;
  float scratchProgress;

  id<ScratchViewDelegate> _delegate;
}

@property(nonatomic, assign) id<ScratchViewDelegate> _delegate;

@property(nonatomic, copy) RCTBubblingEventBlock onImageLoadFinished;
@property(nonatomic, copy) RCTBubblingEventBlock onTouchStateChanged;
@property(nonatomic, copy) RCTBubblingEventBlock onScratchProgressChanged;
@property(nonatomic, copy) RCTBubblingEventBlock onScratchDone;

- (id)init;
- (void)reset;

@end
