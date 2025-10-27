package utils.types

import scala.reflect.ClassTag

class CircularBuffer[A: ClassTag] private (
    private val buffer: Array[A],
    private val start: Int,
    private val end: Int,
    val maxSize: Int,
) extends Iterable[A]:

  def this(maxSize: Int) = this(Array.ofDim[A](maxSize), 0, 0, maxSize)

  def clear: CircularBuffer[A] =
    new CircularBuffer[A](maxSize)

  def add(element: A): CircularBuffer[A] =
    val newBuffer = buffer.clone()
    newBuffer(end) = element
    val newEnd = (end + 1) % maxSize
    val newStart = if newEnd == start then (start + 1) % maxSize else start

    new CircularBuffer[A](newBuffer, newStart, newEnd, maxSize)

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
