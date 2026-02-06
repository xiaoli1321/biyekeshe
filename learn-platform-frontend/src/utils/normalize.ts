import type { Course } from '@/types'

const DIFFICULTY_LABELS: Record<string, string> = {
  BEGINNER: '初级',
  INTERMEDIATE: '中级',
  ADVANCED: '高级'
}

export const normalizeCourse = (course: Course): Course => {
  if (!course) {
    return course
  }

  const title = course.title || course.name || '未命名课程'
  const difficultyLevel = course.difficultyLevel
  const difficulty = course.difficulty || (difficultyLevel ? DIFFICULTY_LABELS[difficultyLevel] : undefined)

  return {
    ...course,
    title,
    name: course.name || title,
    difficulty
  }
}

export const normalizeCourseList = (courses?: Course[]): Course[] => {
  if (!courses || courses.length === 0) {
    return []
  }
  return courses.map(normalizeCourse)
}
