const base = 'http://127.0.0.1:8084/api';
const templateName = '知识图谱全效果模板课';

async function api(path, { method = 'GET', token, body } = {}) {
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json; charset=utf-8';
  }

  const response = await fetch(`${base}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(`${method} ${path} 失败: ${response.status} ${text}`);
  }

  return data;
}

const login = await api('/auth/login', {
  method: 'POST',
  body: { email: 'admin@learning.com', password: 'admin123' }
});

if (!login?.success) {
  throw new Error(`登录失败: ${login?.message ?? '未知错误'}`);
}

const token = login.data.token;

const allCourses = await api('/courses/all', { token });
for (const course of allCourses.data.filter(course => course.name === templateName || course.title === templateName)) {
  await api(`/courses/${course.id}`, { method: 'DELETE', token });
}

const courseRes = await api('/courses', {
  method: 'POST',
  token,
  body: {
    name: templateName,
    description: '用于演示课程结构、章节内容、知识点内容卡片、知识图谱关系、章节分组和学习路径的完整模板课程。',
    difficultyLevel: 'BEGINNER',
    instructor: '系统演示老师',
    estimatedHours: 6,
    tags: '模板,知识图谱,演示,课程设计',
    published: true
  }
});

if (!courseRes.success) {
  throw new Error(`创建课程失败: ${courseRes.message}`);
}

const courseId = courseRes.data.id;

const chapterSpecs = [
  {
    title: '第一章：程序起步',
    description: '先建立运行环境与数据表达的基础认识。',
    content: '这一章负责让学生知道程序从哪里运行、数据如何被保存，以及为什么变量和数据类型是第一批必须掌握的概念。',
    type: 'TEXT',
    estimatedMinutes: 35,
    orderIndex: 1
  },
  {
    title: '第二章：流程控制',
    description: '理解程序如何根据条件做判断、根据任务做重复。',
    content: '这一章关注代码的执行路径，包括条件分支和循环控制。学完后，学生应该能看懂一个简单程序是怎样一步步做决策的。',
    type: 'TEXT',
    estimatedMinutes: 40,
    orderIndex: 2
  },
  {
    title: '第三章：函数与模块',
    description: '把零散语句组织成可以复用的结构。',
    content: '这一章把前面的语句和流程组织起来，引入函数封装和模块协作，帮助学生建立从单句代码到可维护程序的认知。',
    type: 'TEXT',
    estimatedMinutes: 45,
    orderIndex: 3
  }
];

const chapters = [];
for (const spec of chapterSpecs) {
  const chapterRes = await api(`/chapters/course/${courseId}`, {
    method: 'POST',
    token,
    body: spec
  });
  if (!chapterRes.success) {
    throw new Error(`创建章节失败: ${chapterRes.message}`);
  }
  chapters.push(chapterRes.data);
}

const chapterMap = new Map(chapters.map(chapter => [chapter.title, chapter.id]));

const conceptSpecs = [
  {
    chapterTitle: '第一章：程序起步',
    name: '运行环境',
    description: '程序执行所依赖的基础设施。',
    summary: '运行环境决定代码在哪里被加载、解释和执行。',
    content: '运行环境包括解释器、编译器、运行时库和操作系统接口。理解运行环境后，学生会知道为什么同一段代码在不同平台上可能出现不同表现。',
    example: '例如：Java 代码需要先经过编译，再由 JVM 在不同系统上执行。',
    commonPitfall: '把编程语言和运行环境当成同一个东西，往往会导致部署问题定位困难。',
    difficultyLevel: 1,
    importanceWeight: 82
  },
  {
    chapterTitle: '第一章：程序起步',
    name: '变量与类型',
    description: '用统一的方式保存和约束数据。',
    summary: '变量负责存值，类型负责说明这个值可以如何被解释和使用。',
    content: '变量是程序中可命名的数据槽位，类型则规定了数据的取值范围和可参与的操作。二者组合在一起，决定了程序对数据的理解方式。',
    example: '例如：int age = 18; 其中 age 是变量，int 是类型。',
    commonPitfall: '只记住变量名，不理解类型约束，后面在比较、计算和传参时容易出错。',
    difficultyLevel: 1,
    importanceWeight: 92
  },
  {
    chapterTitle: '第二章：流程控制',
    name: '条件分支',
    description: '根据不同条件走不同执行路径。',
    summary: '条件分支让程序具备先判断、再行动的能力。',
    content: '条件分支的核心是布尔判断。程序先对条件求值，再决定执行哪一段逻辑，因此它是从顺序执行迈向动态决策的第一步。',
    example: '例如：如果成绩大于等于 60，就显示及格，否则显示补考。',
    commonPitfall: '只会写 if，不会拆分复杂条件，最后把多个规则堆成难维护的判断语句。',
    difficultyLevel: 2,
    importanceWeight: 88
  },
  {
    chapterTitle: '第二章：流程控制',
    name: '循环控制',
    description: '把重复动作交给程序自动完成。',
    summary: '循环控制用于描述满足条件时重复执行的逻辑。',
    content: '循环不仅是减少重复代码的手段，更是在训练学生用初始化、条件、更新三个阶段去描述过程。掌握循环后，很多批量处理问题都会变得自然。',
    example: '例如：用 for 循环遍历 1 到 10，并累计求和。',
    commonPitfall: '忘记更新循环条件，或者边界判断不清，最常见的结果就是死循环和越界。',
    difficultyLevel: 2,
    importanceWeight: 90
  },
  {
    chapterTitle: '第三章：函数与模块',
    name: '函数封装',
    description: '把一组明确职责的语句封装成可复用单元。',
    summary: '函数封装让代码从能跑走向能复用、能维护。',
    content: '函数的价值不只是少写代码，而是把输入、处理、输出边界说清楚。它让程序从零散语句升级为可组合的能力模块。',
    example: '例如：把计算平均值的逻辑提取为 calculateAverage(scores)。',
    commonPitfall: '把函数写得过长、职责过多，最后只是把混乱代码换了个位置。',
    difficultyLevel: 3,
    importanceWeight: 94
  },
  {
    chapterTitle: '第三章：函数与模块',
    name: '模块协作',
    description: '让多个函数和文件按职责组合成完整程序。',
    summary: '模块协作关注的是边界清晰、依赖明确、职责分离。',
    content: '模块让项目可以被多人协作和渐进扩展。学生需要理解一个模块暴露什么接口、依赖什么能力，以及为什么模块化能降低系统复杂度。',
    example: '例如：一个模块负责数据读取，另一个模块负责计算，最后由主流程统一调用。',
    commonPitfall: '模块划分过细或过粗都会增加理解成本，关键是按职责边界拆分。',
    difficultyLevel: 3,
    importanceWeight: 86
  }
];

const conceptMap = new Map();
for (const spec of conceptSpecs) {
  const { chapterTitle, ...rest } = spec;
  const conceptRes = await api('/concepts', {
    method: 'POST',
    token,
    body: {
      ...rest,
      chapterId: chapterMap.get(chapterTitle),
      courseId
    }
  });
  if (!conceptRes.success) {
    throw new Error(`创建知识点失败(${spec.name}): ${conceptRes.message}`);
  }
  conceptMap.set(spec.name, conceptRes.data.id);
}

const relationshipSpecs = [
  { from: '运行环境', to: '变量与类型', type: 'PREREQUISITE', weight: 0.9, description: '理解代码在哪里运行后，再理解数据如何被存储会更自然。' },
  { from: '变量与类型', to: '条件分支', type: 'DEPENDS_ON', weight: 0.82, description: '条件判断依赖变量取值与类型比较。' },
  { from: '条件分支', to: '循环控制', type: 'SIMILAR_TO', weight: 0.7, description: '二者都属于流程控制，适合对照学习。' },
  { from: '循环控制', to: '函数封装', type: 'USES', weight: 0.76, description: '循环中常常会调用函数，把重复处理抽离出来。' },
  { from: '函数封装', to: '模块协作', type: 'PART_OF', weight: 0.88, description: '函数是模块内部最基础的组织单元。' }
];

for (const spec of relationshipSpecs) {
  const relationshipRes = await api('/relationships', {
    method: 'POST',
    token,
    body: {
      fromConceptId: conceptMap.get(spec.from),
      toConceptId: conceptMap.get(spec.to),
      type: spec.type,
      weight: spec.weight,
      description: spec.description
    }
  });
  if (!relationshipRes.success) {
    throw new Error(`创建关系失败(${spec.from} -> ${spec.to}): ${relationshipRes.message}`);
  }
}

await api(`/courses/${courseId}/toggle-publish`, { method: 'POST', token });

const conceptsRes = await api(`/courses/${courseId}/concepts`, { token });
const relationshipsRes = await api(`/courses/${courseId}/relationships`, { token });
const graphRes = await api(`/graphs/concepts/${courseId}`, { token });

console.log(JSON.stringify({
  courseId,
  courseName: templateName,
  chapterCount: chapters.length,
  conceptCount: Array.isArray(conceptsRes.data) ? conceptsRes.data.length : 0,
  relationshipCount: Array.isArray(relationshipsRes.data) ? relationshipsRes.data.length : 0,
  graphNodeCount: Array.isArray(graphRes.data?.nodes) ? graphRes.data.nodes.length : 0,
  graphLinkCount: Array.isArray(graphRes.data?.links) ? graphRes.data.links.length : 0,
  chapters: chapters.map(chapter => chapter.title)
}, null, 2));
