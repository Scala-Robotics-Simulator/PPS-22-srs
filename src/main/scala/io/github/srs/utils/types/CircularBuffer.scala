package utils.types

import scala.reflect.ClassTag

class CircularBuffer[A: ClassTag](val maxSize: Int) extends Iterable[A]:
  private var buffer = Array.ofDim[A](maxSize)
  private var start = 0
  private var end = 0

  def clear: Unit =
    start = 0
    end = 0
    buffer = Array.ofDim[A](maxSize)

  def add(element: A): Unit =
    buffer(end) = element
    end = (end + 1) % maxSize

    if end == start then start = (start + 1) % maxSize

  def get(index: Int): Option[A] =
    if index >= 0 && index < size then Some(buffer((start + index) % maxSize))
    else None

  override def size: Int =
    if end >= start then end - start
    else maxSize - start + end

  override def isEmpty: Boolean = start == end

  override def iterator: Iterator[A] = new Iterator[A]:
    private var pos = start

    override def hasNext: Boolean = pos != end

    override def next(): A =
      val elem = buffer(pos)
      pos = (pos + 1) % maxSize
      elem
end CircularBuffer
