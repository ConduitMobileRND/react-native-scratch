#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "ScratchViewTools.h"
#import "ScratchView.h"

@implementation ScratchView

-(id)init
{
  self = [super init];
  self.userInteractionEnabled = true;
  self.exclusiveTouch = true;
  [self reset];
  return self;
}

-(id) initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    self.multipleTouchEnabled = NO;
    self.userInteractionEnabled = true;
    self.exclusiveTouch = true;
    [self reset];
  }
  return self;
}


-(void) setPlaceholderColor:(NSString *)colorString
{
  @try {
    placeholderColor = [ScratchViewTools colorFromHexString:colorString];
  }
  @catch (NSException *exception) {
    NSLog(@"placeholderColor error: %@", exception.reason);
  }
  UIColor *backgroundColor = placeholderColor != nil ? placeholderColor : [UIColor grayColor];
  image = [ScratchViewTools createImageFromColor:backgroundColor];
  [self setImage:image];
}

-(void) setImageUrl:(NSString *)url
{
  imageUrl = url;
  [self reset];
}

-(void) setThreshold: (float)value
{
  threshold = value;
}

-(void) setBrushSize: (float)value
{
  brushSize = value;
}

-(void)loadImage
{
  UIColor *backgroundColor = placeholderColor != nil ? placeholderColor : [UIColor grayColor];
  image  = [ScratchViewTools createImageFromColor:backgroundColor];
  [self setImage:image];
  if (imageUrl == nil) {
    return;
  }
  NSURLSessionTask *task = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString: imageUrl] completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
    if (data) {
      self->image = [UIImage imageWithData:data];
    }
    dispatch_sync(dispatch_get_main_queue(), ^{
      [self setImage:self->image];
      [self reportImageLoadFinished: data ? true : false];
    });
  }];
  [task resume];
}

-(void) reset {
  [self initGrid];
  [self loadImage];
  threshold = threshold > 0 ? threshold : 50;
  path = nil;
  [self reportScratchProgress];
  [self reportScratchState];
}

-(void) initGrid
{
  int gridSize = sizeof(grid[0]);
  for (int x = 0; x < gridSize; x++)
  {
    for (int y = 0; y < gridSize; y++)
    {
      grid[x][y] = true;
    }
  }
  clearPointsCounter = 0;
  cleared = false;
  scratchProgress = 0;
}

-(void) updateGrid: (CGPoint)point
{
  float gridSize = (float)sizeof(grid[0]);
  float viewWidth = self.frame.size.width;
  float viewHeight = self.frame.size.height;
  float pointInGridX = (MAX(MIN(point.x, viewWidth), 0) / viewWidth) * gridSize;
  float pointInGridY = (MAX(MIN(point.y, viewWidth), 0) / viewHeight) * gridSize;
  if (grid[(int)pointInGridX][(int)pointInGridY] == true) {
    grid[(int)pointInGridX][(int)pointInGridY] = false;
    clearPointsCounter++;
    scratchProgress = ((float)clearPointsCounter) / ((float)(gridSize*gridSize)) * 100;
    [self reportScratchProgress];
  }
}

-(void) touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
  [self reportTouchState:true];
  UITouch *touch = [touches anyObject];
  path = [UIBezierPath bezierPath];
  path.lineWidth = brushSize > 0 ? brushSize : (self.frame.size.height < self.frame.size.width ? self.frame.size.height : self.frame.size.width) / 10.0f;
  
  CGPoint point = [touch locationInView:self];
  [path moveToPoint:point];
  
  CGSize imgSize = self.frame.size;
  CGFloat scale = 0;
  UIGraphicsBeginImageContextWithOptions(self.frame.size, NO, scale);
  [image drawInRect:CGRectMake(0, 0, imgSize.width, imgSize.height)];
}

-(void) touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
  UITouch *touch = [touches anyObject];
  CGPoint point = [touch locationInView:self];
  [path addLineToPoint:point];
  [self updateGrid: point];
  if (!cleared && scratchProgress > threshold) {
    cleared = true;
    [self reportScratchState];
  }
  [self drawImage];
}

-(void) touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
  if (path == nil)
  {
    return
  }
  [self reportTouchState:false];
  image = [self drawImage];
  UIGraphicsEndImageContext();
  path = nil;
}

-(void) touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event
{
  if (path == nil)
  {
    return
  }
  [self reportTouchState:false];
  image = [self drawImage];
  UIGraphicsEndImageContext();
  path = nil;
}

- (UIImage *) drawImage
{
  if (path != nil) {
    [path strokeWithBlendMode:kCGBlendModeClear alpha:0];
  }
  UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
  [self setImage:newImage];
  return newImage;
}

-(void) reportImageLoadFinished:(BOOL)success {
  [self._delegate onImageLoadFinished:self successState:success];
}

-(void) reportTouchState:(BOOL)state {
  [self._delegate onTouchStateChanged:self touchState:state];
}

-(void) reportScratchProgress
{
  [self._delegate onScratchProgressChanged:self didChangeProgress:scratchProgress];
}

-(void) reportScratchState {
  [self._delegate onScratchDone:self isScratchDone:cleared];
}

@end
