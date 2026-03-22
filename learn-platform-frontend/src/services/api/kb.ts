import http from '../http';

// Use same logic as http.ts to ensure consistency for fetch (used for streaming)
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export interface KbCollection {
  id: string;
  name: string;
  description: string;
  userId: string;
  createdAt: string;
}

export interface KbDocument {
  id: string;
  filename: string;
  fileType: string;
  collectionId: string;
  uploadDate: string;
  status: 'PARSING' | 'READY' | 'ERROR';
}

export interface KbChunk {
  id: string;
  documentId: string;
  content: string;
  chunkIndex: number;
  vectorStatus: string;
}

export const kbApi = {
  // --- 知识库空间管理 ---
  async fetchCollections(): Promise<KbCollection[]> {
    const res: any = await http.get('/kb/collections');
    return res.data;
  },

  async createCollection(name: string, description: string): Promise<KbCollection> {
    const res: any = await http.post('/kb/collections', { name, description });
    return res.data;
  },

  async deleteCollection(id: string): Promise<void> {
    await http.delete(`/kb/collections/${id}`);
  },

  // --- 文档管理 ---
  async fetchDocuments(collectionId: string): Promise<KbDocument[]> {
    const res: any = await http.get('/kb/documents', {
      params: { collectionId }
    });
    return res.data;
  },

  async uploadFile(file: File, collectionId: string): Promise<KbDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('collectionId', collectionId);
    const res: any = await http.post('/kb/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return res.data;
  },

  async submitText(title: string, content: string, collectionId: string): Promise<KbDocument> {
    const res: any = await http.post('/kb/documents/text', { title, content, collectionId });
    return res.data;
  },

  async deleteDocument(id: string): Promise<void> {
    await http.delete(`/kb/documents/${id}`);
  },

  async fetchChunks(docId: string): Promise<KbChunk[]> {
    const res: any = await http.get(`/kb/documents/${docId}/chunks`);
    return res.data;
  },

  // --- RAG 问答 ---
  async fetchKbChatStream(message: string, collectionId: string, onMessage: (chunk: string) => void) {
    const token = localStorage.getItem('token');
    
    try {
      const normalizedBaseUrl = BASE_URL.endsWith('/') ? BASE_URL.slice(0, -1) : BASE_URL;
      
      const response = await fetch(`${normalizedBaseUrl}/kb/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify({ message, collectionId })
      });

      if (!response.ok) {
        if (response.status === 401) throw new Error('登录已失效，请重新登录');
        throw new Error('知识库问答服务调用失败');
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      if (reader) {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          
          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('data: ')) {
              try {
                const data = JSON.parse(line.slice(6));
                if (data.type === 'token') {
                  onMessage(data.content);
                }
              } catch (e) {
                console.warn('Failed to parse SSE line:', line);
              }
            }
          }
        }
      }
    } catch (error) {
      console.error('Chat stream error:', error);
      throw error;
    }
  }
};
